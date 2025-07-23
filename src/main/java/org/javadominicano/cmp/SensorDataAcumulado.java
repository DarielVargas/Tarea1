package org.javadominicano.cmp;

import com.google.gson.JsonObject;

public class SensorDataAcumulado {
    public String fecha;
    public String estacionId;  // Ejemplo: "estacion-1"

    public String sensor_temperatura;
    public String sensor_humedad;
    public String sensor_presion;
    public String sensor_direccion;
    public String sensor_velocidad;
    public String sensor_precipitacion;
    public String sensor_humedad_suelo;

    public Double temperatura;
    public Double humedad;
    public Double presion;
    public Double velocidad;
    public Double precipitacion;
    public Double humedad_suelo;
    public String direccion;

    public boolean estaCompleto() {
        return temperatura != null &&
               humedad != null &&
               presion != null &&
               velocidad != null &&
               precipitacion != null &&
               humedad_suelo != null &&
               direccion != null;
    }

    public void reiniciar() {
        temperatura = null;
        humedad = null;
        presion = null;
        velocidad = null;
        precipitacion = null;
        humedad_suelo = null;
        direccion = null;

        sensor_temperatura = null;
        sensor_humedad = null;
        sensor_presion = null;
        sensor_direccion = null;
        sensor_velocidad = null;
        sensor_precipitacion = null;
        sensor_humedad_suelo = null;

        fecha = null;
        estacionId = null;
    }

    // ✅ Genera el JSON con el formato exacto requerido por el API
    //    Incluye todos los sensores disponibles cuando el conjunto de datos
    //    está completo.
    public String toJsonApi() {
        JsonObject json = new JsonObject();
        json.addProperty("grupo", "1");

        // Extraer solo el número de la estación (por ejemplo: "1" de "estacion-1")
        String estacionNumero = (estacionId != null && estacionId.contains("-"))
                ? estacionId.split("-")[1]
                : estacionId;

        json.addProperty("estacion", estacionNumero);
        json.addProperty("fecha", fecha);
        json.addProperty("temperatura", temperatura);
        json.addProperty("humedad", humedad);
        json.addProperty("presion", presion);
        json.addProperty("velocidad", velocidad);
        json.addProperty("direccion", direccion);
        json.addProperty("precipitacion", precipitacion);
        json.addProperty("humedad_suelo", humedad_suelo);
        return json.toString();
    }

    // Setters
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }
    public void setHumedad(Double humedad) { this.humedad = humedad; }
    public void setPresion(Double presion) { this.presion = presion; }
    public void setVelocidad(Double velocidad) { this.velocidad = velocidad; }
    public void setPrecipitacion(Double precipitacion) { this.precipitacion = precipitacion; }
    public void setHumedadSuelo(Double humedad_suelo) { this.humedad_suelo = humedad_suelo; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setEstacionId(String estacionId) { this.estacionId = estacionId; }
}
