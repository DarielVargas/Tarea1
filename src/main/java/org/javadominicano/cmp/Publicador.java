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

        // Humedad cada 6 segundos
        new Thread(() -> {
            while (true) {
                Sensor s = new Sensor("sensor3", "humedad");
                new Publicador("pub-humedad").enviarMensaje(
                        "/itt363-grupo1/estacion-1/sensores/humedad", gson.toJson(s));
                esperar(6000);
            }
        }).start();

        // Temperatura cada 4 segundos
        new Thread(() -> {
            while (true) {
                Sensor s = new Sensor("sensor4", "temperatura");
                new Publicador("pub-temperatura").enviarMensaje(
                        "/itt363-grupo1/estacion-1/sensores/temperatura", gson.toJson(s));
                esperar(4000);
            }
        }).start();

        // Precipitación cada 8 segundos
        new Thread(() -> {
            while (true) {
                Sensor s = new Sensor("sensor5", "precipitacion");
                new Publicador("pub-precipitacion").enviarMensaje(
                        "/itt363-grupo1/estacion-1/sensores/precipitacion", gson.toJson(s));
                esperar(8000);
            }
        }).start();

        // Presión atmosférica cada 9 segundos
        new Thread(() -> {
            while (true) {
                Sensor s = new Sensor("sensor6", "presion");
                new Publicador("pub-presion").enviarMensaje(
                        "/itt363-grupo1/estacion-1/sensores/presion", gson.toJson(s));
                esperar(9000);
            }
        }).start();

        // Humedad del suelo cada 10 segundos
        new Thread(() -> {
            while (true) {
                Sensor s = new Sensor("sensor7", "humedad_suelo");
                new Publicador("pub-humedad-suelo").enviarMensaje(
                        "/itt363-grupo1/estacion-1/sensores/humedad_suelo", gson.toJson(s));
                esperar(10000);
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
