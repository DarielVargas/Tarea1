package org.javadominicano.cmp;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Publicador {
    private static final String BROKER_URL = "tcp://mqtt.eict.ce.pucmm.edu.do:1883";
    private MqttClient client;

    public Publicador(String id) {
        try {
            client = new MqttClient(BROKER_URL, id);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void enviarMensaje(String topic, String mensaje) {
        try {
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setAutomaticReconnect(true);
            connectOptions.setCleanSession(false);
            connectOptions.setUserName("itt363-grupo1");
            connectOptions.setPassword("myhZkhrv2m5Y".toCharArray());

            client.connect(connectOptions);
            client.publish(topic, mensaje.getBytes(), 2, false);
            client.disconnect();
            client.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void iniciarPrueba() throws Exception {
        Gson gson = new Gson();

        // Velocidad del viento cada 5 segundos
        new Thread(() -> {
            while (true) {
                Sensor s = new Sensor("sensor1", "velocidad");
                new Publicador("pub-velocidad").enviarMensaje(
                        "/itt363-grupo1/estacion-1/sensores/velocidad", gson.toJson(s));
                esperar(5000);
            }
        }).start();

        // Dirección del viento cada 7 segundos
        new Thread(() -> {
            while (true) {
                Sensor s = new Sensor("sensor2", "direccion");
                new Publicador("pub-direccion").enviarMensaje(
                        "/itt363-grupo1/estacion-1/sensores/direccion", gson.toJson(s));
                esperar(7000);
            }
        }).start();

        // Probabilidad de lluvia cada 9 segundos
        new Thread(() -> {
            while (true) {
                Sensor s = new Sensor("sensor3", "precipitacion");
                new Publicador("pub-precipitacion").enviarMensaje(
                        "/itt363-grupo1/estacion-1/sensores/probabilidad", gson.toJson(s)); // ← topic corregido aquí
                esperar(9000);
            }
        }).start();
    }

    private static void esperar(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
