package org.javadominicano.cmp;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class Main {
    public static void main(String[] args) {
        // 🚫 Quitamos el simulador
        // try {
        //     Publicador.iniciarPrueba();
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        String broker = "tcp://mqtt.eict.ce.pucmm.edu.do:1883";
        String usuario = "itt363-grupo1";
        String contrasena = "myhZkhrv2m5Y";

        try {
            MqttClient client = new MqttClient(broker, MqttClient.generateClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(usuario);
            options.setPassword(contrasena.toCharArray());

            client.setCallback(new SuscriptorCallback());
            client.connect(options);

            // Suscribimos cada topic individualmente
            //client.subscribe("/itt363-grupo1/estacion-1/sensores/probabilidad");
            client.subscribe("/itt363-grupo1/estacion-1/sensores/temperatura");
            client.subscribe("/itt363-grupo1/estacion-1/sensores/presion");
            client.subscribe("/itt363-grupo1/estacion-1/sensores/humedad");
            client.subscribe("/itt363-grupo1/estacion-1/sensores/precipitacion");
            //client.subscribe("/itt363-grupo1/estacion-1/sensores/datos");
            //client.subscribe("/itt363-grupo1/estacion-2/sensores/datos");
            client.subscribe("/itt363-grupo1/estacion-1/sensores/velocidad");
            client.subscribe("/itt363-grupo1/estacion-1/sensores/direccion");

            System.out.println("✅ Suscriptor activo. Esperando datos...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
