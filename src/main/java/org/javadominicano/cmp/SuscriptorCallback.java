package org.javadominicano.cmp;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SuscriptorCallback implements MqttCallback {

    private static final String JDBC_URL = "jdbc:mariadb://192.168.100.166:3306/estacion_mqtt";
    private static final String DB_USER = "mqttuser";
    private static final String DB_PASS = "claveMQTT123";

    private final Gson gson = new Gson();
    private final SensorDataAcumulado acumulado = new SensorDataAcumulado();

    private final BlockingQueue<SensorDataWrapper> cola = new LinkedBlockingQueue<>();

    public SuscriptorCallback() {
        iniciarProcesador();
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Conexión perdida con el broker: " + cause.getMessage());
        System.out.println("Intentando reconectar...");
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    Main.reconectarCliente();
                    System.out.println("Reconexión exitosa.");
                    break;
                } catch (Exception e) {
                    System.out.println("Fallo al reconectar. Reintentando en 5 segundos...");
                }
            }
        }).start();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        SensorDataWrapper wrapper = new SensorDataWrapper(topic, message.toString());
        cola.offer(wrapper);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Información Recibida.");
    }

    // Clase interna para envolver topic + mensaje
    private static class SensorDataWrapper {
        String topic;
        String mensaje;

        SensorDataWrapper(String topic, String mensaje) {
            this.topic = topic;
            this.mensaje = mensaje;
        }
    }

    private void iniciarProcesador() {
        new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
                while (true) {
                    SensorDataWrapper wrapper = cola.take();  // Bloquea si está vacía
                    procesarMensaje(wrapper.topic, wrapper.mensaje, conn);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Procesador-MQTT").start();
    }

    private void procesarMensaje(String topic, String mensaje, Connection conn) {
        try {
            SensorData datos = gson.fromJson(mensaje, SensorData.class);

            if (datos.valor == null || datos.valor.toString().equalsIgnoreCase("nan")) {
                System.out.println("Valor inválido (NaN) para tipo: " + datos.tipo);
                return;
            }

            SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date parsedDate = formatoEntrada.parse(datos.fecha.trim());
            Timestamp fechaSQL = new Timestamp(parsedDate.getTime());
            String fechaApi = formatoEntrada.format(parsedDate);

            String[] partes = topic.split("/");
            if (partes.length < 5) return;

            String estacionId = partes[2];
            String tipo = partes[4];

            guardarEstacion(conn, estacionId);

            String sql;
            switch (tipo) {
                case "velocidad":
                    sql = "INSERT INTO datos_velocidad (sensor_id, estacion_id, velocidad, fecha) VALUES (?, ?, ?, ?)";
                    acumulado.setVelocidad(parseDouble(datos.valor));
                    acumulado.sensor_velocidad = datos.sensorId;
                    break;
                case "direccion":
                    sql = "INSERT INTO datos_direccion (sensor_id, estacion_id, direccion, fecha) VALUES (?, ?, ?, ?)";
                    acumulado.setDireccion(datos.valor.toString());
                    acumulado.sensor_direccion = datos.sensorId;
                    break;
                case "humedad":
                    sql = "INSERT INTO datos_humedad (sensor_id, estacion_id, humedad, fecha) VALUES (?, ?, ?, ?)";
                    acumulado.setHumedad(parseDouble(datos.valor));
                    acumulado.sensor_humedad = datos.sensorId;
                    break;
                case "temperatura":
                    sql = "INSERT INTO datos_temperatura (sensor_id, estacion_id, temperatura, fecha) VALUES (?, ?, ?, ?)";
                    acumulado.setTemperatura(parseDouble(datos.valor));
                    acumulado.sensor_temperatura = datos.sensorId;
                    break;
                case "precipitacion":
                    sql = "INSERT INTO datos_precipitacion (sensor_id, estacion_id, probabilidad, fecha) VALUES (?, ?, ?, ?)";
                    acumulado.setPrecipitacion(parseDouble(datos.valor));
                    acumulado.sensor_precipitacion = datos.sensorId;
                    break;
                case "presion":
                    sql = "INSERT INTO datos_presion (sensor_id, estacion_id, valor, fecha) VALUES (?, ?, ?, ?)";
                    acumulado.setPresion(parseDouble(datos.valor));
                    acumulado.sensor_presion = datos.sensorId;
                    break;
                case "humedad_suelo":
                    sql = "INSERT INTO datos_humedad_suelo (sensor_id, estacion_id, valor, fecha) VALUES (?, ?, ?, ?)";
                    acumulado.setHumedadSuelo(parseDouble(datos.valor));
                    acumulado.sensor_humedad_suelo = datos.sensorId;
                    break;
                default:
                    System.out.println("Tipo de sensor desconocido: " + tipo);
                    return;
            }

            acumulado.setFecha(fechaApi);
            acumulado.setEstacionId(estacionId);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, datos.sensorId);
                stmt.setString(2, estacionId);
                if (tipo.equals("direccion")) {
                    stmt.setString(3, datos.valor.toString());
                } else {
                    stmt.setDouble(3, parseDouble(datos.valor));
                }
                stmt.setTimestamp(4, fechaSQL);
                stmt.executeUpdate();
                System.out.println("Insert exitoso en [" + tipo + "] con " + datos.sensorId + ", " + datos.valor);
            }

            if (acumulado.estaCompleto()) {
                String jsonApi = acumulado.toJsonApi();
                System.out.println("JSON a enviar al API: " + jsonApi);
                ApiClient.enviarDatos(jsonApi);
                acumulado.reiniciar();
            }

        } catch (Exception e) {
            System.out.println("❌ Error al procesar mensaje MQTT");
            e.printStackTrace();
        }
    }

    private double parseDouble(Object valor) {
        return Double.parseDouble(valor.toString().replace(",", "."));
    }

    private void guardarEstacion(Connection conn, String estacionId) {
        String nombre = "Estacion " + estacionId.replace("estacion-", "");
        String ubicacion = "Desconocida";
        String sql = "INSERT INTO estaciones (id, nombre, ubicacion) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), ubicacion=VALUES(ubicacion)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estacionId);
            stmt.setString(2, nombre);
            stmt.setString(3, ubicacion);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error al guardar estación: " + estacionId);
            e.printStackTrace();
        }
    }
}
