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
import java.util.Locale;

public class SuscriptorCallback implements MqttCallback {

    private static final String JDBC_URL = "jdbc:mariadb://192.168.100.166:3306/estacion_mqtt";
    private static final String DB_USER = "mqttuser";
    private static final String DB_PASS = "claveMQTT123";

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Conexión Perdida...");
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

        // Limpieza de caracteres invisibles o malformateados en la fecha
        String fechaAjustada = datos.fecha
            .replaceAll("[^\\x20-\\x7E]", "")
            .replaceAll("(?i)\\s*p\\.?\\s*m\\.?", " PM")
            .replaceAll("(?i)\\s*a\\.?\\s*m\\.?", " AM")
            .replaceAll("\\s+", " ")
            .trim();

        Timestamp fechaSQL;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, h:mm:ss a", Locale.ENGLISH);
            Date parsedDate = dateFormat.parse(fechaAjustada);
            fechaSQL = new Timestamp(parsedDate.getTime());
        } catch (Exception e) {
            System.out.println("Error al convertir fecha ajustada: " + fechaAjustada);
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

        String sql;
        switch (tipo) {
            case "velocidad":
                sql = "INSERT INTO datos_velocidad (sensor_id, estacion_id, velocidad, fecha) VALUES (?, ?, ?, ?)";
                break;
            case "direccion":
                sql = "INSERT INTO datos_direccion (sensor_id, estacion_id, direccion, fecha) VALUES (?, ?, ?, ?)";
                break;
            case "humedad":
                sql = "INSERT INTO datos_humedad (sensor_id, estacion_id, humedad, fecha) VALUES (?, ?, ?, ?)";
                break;
            case "temperatura":
                sql = "INSERT INTO datos_temperatura (sensor_id, estacion_id, temperatura, fecha) VALUES (?, ?, ?, ?)";
                break;
            case "precipitacion":
                sql = "INSERT INTO datos_precipitacion (sensor_id, estacion_id, probabilidad, fecha) VALUES (?, ?, ?, ?)";
                break;
            case "presion":
                sql = "INSERT INTO datos_presion (sensor_id, estacion_id, valor, fecha) VALUES (?, ?, ?, ?)";
                break;
            case "humedad_suelo":
                sql = "INSERT INTO datos_humedad_suelo (sensor_id, estacion_id, valor, fecha) VALUES (?, ?, ?, ?)";
                break;
            default:
                System.out.println("Tipo de sensor desconocido: " + tipo);
                return;
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, datos.sensorId);
            stmt.setString(2, estacionId);

            if (tipo.equals("direccion")) {
                stmt.setString(3, datos.valor.toString());
            } else {
                String valorNumerico = datos.valor.toString().replace(",", ".");
                stmt.setDouble(3, Double.parseDouble(valorNumerico));
            }

            stmt.setTimestamp(4, fechaSQL);
            stmt.executeUpdate();

            System.out.println("Insert exitoso en tabla [" + tipo + "] con: " + datos.sensorId + ", " + datos.valor + ", " + fechaSQL);
        } catch (Exception e) {
            System.out.println("Error al insertar en la tabla [" + tipo + "]");
            e.printStackTrace();
        }
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
            System.out.println("✅ Inserción de estación realizada correctamente.");
        } catch (Exception e) {
            System.out.println("Error al guardar estación: " + estacionId);
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Información Recibida.");
    }
}
