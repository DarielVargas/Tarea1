package org.javadominicano.cmp;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class Main {
    public static void main(String[] args) {
        // 👉 Esto inicia los hilos de publicación
        try {
            Publicador.iniciarPrueba();  // << LÍNEA QUE FALTABA
        } catch (Exception e) {
            e.printStackTrace();
        }

        String broker = "tcp://mqtt.eict.ce.pucmm.edu.do:1883";
        String usuario = "itt363-grupo1";
        String contrasena = "myhZkhrv2m5Y";

        try {
            MqttClient client = new MqttClient(broker, MqttClient.generateClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(usuario);
            options.setPassword(contrasena.toCharArray());

            client.setCallback(new SuscriptorCallback()); // Usamos 1 sola clase para todo
            client.connect(options);

            // Escuchar todos los sensores
            client.subscribe("/itt363-grupo1/estacion-1/sensores/#");

            System.out.println("✅ Suscriptor activo. Esperando datos...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
