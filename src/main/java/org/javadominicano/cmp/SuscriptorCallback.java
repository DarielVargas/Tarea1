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

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Conexión Perdida...");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("\nMensaje recibido desde topic: " + topic);
        System.out.println("Contenido crudo: " + message);

        String jdbcUrl = "jdbc:mariadb://192.168.100.166:3306/estacion_mqtt";
        String usuario = "mqttuser";
        String contrasena = "claveMQTT123";

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

        // Extraer tipo desde el topic
        String[] partes = topic.split("/");
        if (partes.length < 5) {
            System.out.println("Topic no tiene el formato esperado.");
            return;
        }

        String tipo = partes[4];

        String sql;
        switch (tipo) {
            case "velocidad":
                sql = "INSERT INTO datos_velocidad (sensor_id, velocidad, fecha) VALUES (?, ?, ?)";
                break;
            case "direccion":
                sql = "INSERT INTO datos_direccion (sensor_id, direccion, fecha) VALUES (?, ?, ?)";
                break;
            case "humedad":
                sql = "INSERT INTO datos_humedad (sensor_id, humedad, fecha) VALUES (?, ?, ?)";
                break;
            case "temperatura":
                sql = "INSERT INTO datos_temperatura (sensor_id, temperatura, fecha) VALUES (?, ?, ?)";
                break;
            case "precipitacion":
                sql = "INSERT INTO datos_precipitacion (sensor_id, precipitacion, fecha) VALUES (?, ?, ?)";
                break;
            default:
                System.out.println("Tipo de sensor desconocido: " + tipo);
                return;
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl, usuario, contrasena)) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, datos.sensorId);

            if (tipo.equals("direccion")) {
                stmt.setString(2, datos.valor.toString());
            } else {
                // Corregir valores decimales con coma
                String valorNumerico = datos.valor.toString().replace(",", ".");
                stmt.setDouble(2, Double.parseDouble(valorNumerico));
            }

            stmt.setTimestamp(3, fechaSQL);
            stmt.executeUpdate();

            System.out.println("Insert exitoso en tabla [" + tipo + "] con: " + datos.sensorId + ", " + datos.valor + ", " + fechaSQL);
        } catch (Exception e) {
            System.out.println("Error al insertar en la tabla [" + tipo + "]");
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Información Recibida.");
    }
}
