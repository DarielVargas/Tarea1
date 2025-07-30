package org.javadominicano.cmp;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    private static final String API_URL = "https://itt363-hub.smar.com.do/api/";
    private static final String SEGURIDAD_TOKEN = "bXpGELFUbV9U";  // Grupo #1

    public static void enviarDatos(String jsonData) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("SEGURIDAD-TOKEN", SEGURIDAD_TOKEN);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int codigoRespuesta = conn.getResponseCode();
            System.out.println("Datos enviados exitosamente al API.");
            System.out.println("API Codigo de respuesta: " + codigoRespuesta);

            conn.disconnect();

        } catch (Exception e) {
            System.out.println("Error al enviar datos al API:");
            e.printStackTrace();
        }
    }
}
