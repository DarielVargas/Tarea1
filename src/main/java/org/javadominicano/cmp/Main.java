package org.javadominicano.cmp;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class Main {

    private static MqttClient client;
    private static final String broker = "tcp://mqtt.eict.ce.pucmm.edu.do:1883";
    private static final String usuario = "itt363-grupo1";
    private static final String contrasena = "myhZkhrv2m5Y";

    public static void main(String[] args) {
        //try {
        //              Publicador.iniciarPrueba();
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

        try {
            client = new MqttClient(broker, MqttClient.generateClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(usuario);
            options.setPassword(contrasena.toCharArray());

            client.setCallback(new SuscriptorCallback());
            client.connect(options);

            suscribirseATopics();

            System.out.println(" Suscriptor activo. Esperando datos...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔁 Método llamado desde SuscriptorCallback si se pierde conexión
    public static void reconectarCliente() throws Exception {
        if (client != null && !client.isConnected()) {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(usuario);
            options.setPassword(contrasena.toCharArray());

            client.connect(options);
            suscribirseATopics();
        }
    }

    // 📡 Re-suscribirse a todos los topics
    private static void suscribirseATopics() throws Exception {
        client.subscribe("/itt363-grupo1/estacion-1/sensores/temperatura");
        client.subscribe("/itt363-grupo1/estacion-1/sensores/presion");
        client.subscribe("/itt363-grupo1/estacion-1/sensores/humedad");
        client.subscribe("/itt363-grupo1/estacion-1/sensores/humedad_suelo");
        client.subscribe("/itt363-grupo1/estacion-1/sensores/precipitacion");
        client.subscribe("/itt363-grupo1/estacion-1/sensores/velocidad");
        client.subscribe("/itt363-grupo1/estacion-1/sensores/direccion");
    }
}
