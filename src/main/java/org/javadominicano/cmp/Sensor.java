package org.javadominicano.cmp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Sensor {
    private String sensorId;
    private String tipo;  // velocidad, direccion o precipitacion
    private String valor; // puede ser número o texto, se interpreta luego
    private String fecha;

    public Sensor(String sensorId, String tipo) {
        this.sensorId = sensorId;
        this.tipo = tipo;

        Random random = new Random();
        if (tipo.equals("velocidad")) {
            this.valor = String.valueOf(random.nextDouble() * 100); // velocidad del viento en km/h
        } else if (tipo.equals("direccion")) {
            String[] direcciones = {"Norte", "Sur", "Este", "Oeste", "Noreste", "Noroeste", "Sureste", "Suroeste"};
            this.valor = direcciones[random.nextInt(direcciones.length)];
        } else if (tipo.equals("precipitacion")) {
            this.valor = String.valueOf(random.nextDouble() * 100); // probabilidad en %
        }

        // Obtener fecha actual con formato tipo: May 28, 2025, 3:46:54PM
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, h:mm:ssa");
        this.fecha = sdf.format(new Date());
    }

    // Getters
    public String getSensorId() {
        return sensorId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public String getFecha() {
        return fecha;
    }
}
