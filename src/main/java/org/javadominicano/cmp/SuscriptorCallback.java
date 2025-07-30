package org.javadominicano.cmp;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SuscriptorCallback implements MqttCallback {

    private static final String JDBC_URL = "jdbc:mariadb://192.168.100.166:3306/estacion_mqtt";
    private static final String DB_USER = "mqttuser";
    private static final String DB_PASS = "claveMQTT123";

    private final SensorDataAcumulado acumulado = new SensorDataAcumulado();

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Conexión perdida con el broker: " + cause.getMessage());
        System.out.println("Intentando reconectar...");

        new Thread(() -> {
            boolean reconectado = false;

            while (!reconectado) {
                try {
                    Thread.sleep(5000); // Espera 5 segundos
                    Main.reconectarCliente(); // Método que debe estar en Main.java
                    reconectado = true;
                    System.out.println("Reconexión exitosa.");
                } catch (Exception e) {
                    System.out.println("Fallo al reconectar. Reintentando en 5 segundos...");
                }
            }
        }).start();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("\nMensaje recibido desde topic: " + topic);
        System.out.println("Contenido crudo: " + message);

        Gson gson = new Gson();
        SensorData datos;
        try {
            datos = gson.fromJson(message.toString(), SensorData.class);
        } catch (Exception e) {
            System.out.println("Error al parsear JSON:");
            e.printStackTrace();
            return;
        }

        System.out.println("JSON parseado → sensorId: " + datos.sensorId + ", tipo: " + datos.tipo + ", valor: " + datos.valor + ", fecha: " + datos.fecha);

        // Validar valor numérico
        if (datos.valor == null || datos.valor.toString().equalsIgnoreCase("nan")) {
            System.out.println("Valor inválido (NaN) para tipo: " + datos.tipo + ", se descarta el mensaje.");
            return;
        }

        // Convertir la fecha
        Timestamp fechaSQL;
        String fechaApi;
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date parsedDate = formatoEntrada.parse(datos.fecha.trim());
            fechaSQL = new Timestamp(parsedDate.getTime());
            fechaApi = formatoEntrada.format(parsedDate);
        } catch (Exception e) {
            System.out.println("Error al convertir fecha recibida: " + datos.fecha);
            e.printStackTrace();
            return;
        }

        // Extraer tipo y estación desde el topic
        String[] partes = topic.split("/");
        if (partes.length < 5) {
            System.out.println("Topic no tiene el formato esperado.");
            return;
        }

        String estacionId = partes[2];
        guardarEstacion(estacionId);

        String tipo = partes[4];

        // Insertar en base de datos
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

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, datos.sensorId);
            stmt.setString(2, estacionId);

            if (tipo.equals("direccion")) {
                stmt.setString(3, datos.valor.toString());
            } else {
                stmt.setDouble(3, parseDouble(datos.valor));
            }

            stmt.setTimestamp(4, fechaSQL);
            stmt.executeUpdate();

            System.out.println("Insert exitoso en tabla [" + tipo + "] con: " + datos.sensorId + ", " + datos.valor + ", " + fechaSQL);
        } catch (Exception e) {
            System.out.println("Error al insertar en la tabla [" + tipo + "]");
            e.printStackTrace();
        }

        // Enviar al API solo si está completo
        if (acumulado.estaCompleto()) {
            String jsonApi = acumulado.toJsonApi();
            System.out.println("JSON a enviar al API: " + jsonApi);
            ApiClient.enviarDatos(jsonApi);
            acumulado.reiniciar();
        }
    }

    private double parseDouble(Object valor) {
        return Double.parseDouble(valor.toString().replace(",", "."));
    }

    private void guardarEstacion(String estacionId) {
        String nombre = "Estacion " + estacionId.replace("estacion-", "");
        String ubicacion = "Desconocida";

        String sql = "INSERT INTO estaciones (id, nombre, ubicacion) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), ubicacion=VALUES(ubicacion)";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, estacionId);
            stmt.setString(2, nombre);
            stmt.setString(3, ubicacion);
            stmt.executeUpdate();
            System.out.println("Insercion de estacion realizada correctamente.");
        } catch (Exception e) {
            System.out.println("Error al guardar estacion: " + estacionId);
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Información Recibida.");
    }
}
