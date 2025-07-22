package org.javadominicano.cmp;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ApiClient {
    public static void enviarDatos(String grupo,
                                   String estacion,
                                   Date fecha,
                                   Double temperatura,
                                   Double humedad,
                                   Double velocidadViento,
                                   String direccionViento,
                                   Double precipitacion) {
        try {
            String url = "https://itt363-hub.smar.com.do/api/";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("SEGURIDAD-TOKEN", "bXpGELFUbV9U");
            conn.setDoOutput(true);

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaStr = df.format(fecha);

            String json = String.format(
                    "{\"grupo\":\"%s\",\"estacion\":\"%s\",\"fecha\":\"%s\",\"temperatura\":%s,\"humedad\":%s,\"velocidad_viento\":%s,\"direccion_viento\":\"%s\",\"precipitacion\":%s}",
                    grupo, estacion, fechaStr,
                    temperatura, humedad, velocidadViento, direccionViento, precipitacion);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Código de respuesta: " + responseCode);
            if (responseCode == 200 || responseCode == 201) {
                System.out.println("✅ Envío exitoso");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
