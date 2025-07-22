package org.javadominicano.cmp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Sensor {
    private String sensorId;
    // velocidad, direccion, humedad, temperatura, precipitacion, presion y humedad_suelo
    private String tipo;
    private String valor;
    private String fecha;

    public Sensor(String sensorId, String tipo) {
        this.sensorId = sensorId;
        this.tipo = tipo;

        Random random = new Random();
        switch (tipo) {
            case "velocidad":
                this.valor = String.format("%.2f", random.nextDouble() * 100); break;
            case "direccion":
                String[] direcciones = {"Norte", "Sur", "Este", "Oeste", "Noreste", "Noroeste", "Sureste", "Suroeste"};
                this.valor = direcciones[random.nextInt(direcciones.length)]; break;
            case "humedad":
                this.valor = String.format("%.2f", 20 + random.nextDouble() * 80); break;
            case "temperatura":
                this.valor = String.format("%.2f", 15 + random.nextDouble() * 25); break;
            case "precipitacion":
                this.valor = String.format("%.2f", random.nextDouble() * 50); break;
            case "presion":
                this.valor = String.format("%.2f", 990 + random.nextDouble() * 40); break;
            case "humedad_suelo":
                this.valor = String.format("%.2f", random.nextDouble() * 100); break;
            default:
                this.valor = "0";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.fecha = sdf.format(new Date());
    }

    public String getSensorId() { return sensorId; }
    public String getTipo() { return tipo; }
    public String getValor() { return valor; }
    public String getFecha() { return fecha; }
}