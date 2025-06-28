## Proyecto Demo MQTT

Demostración sobre el uso MQTT utilizando Java,
creando Publicadores y Subcriptores mediante el servidor externo de
Mosquitto.

Cada medición recibida ahora guarda el identificador de la estación en la base
de datos. El nombre de la estación se obtiene de la segunda parte del topic
MQTT (por ejemplo `estacion-1`). Las tablas `datos_velocidad`,
`datos_direccion`, `datos_humedad`, `datos_temperatura` y
`datos_precipitacion` incluyen una nueva columna `estacion_id`.

### Ejecutar directamente desde los fuentes

Es necesario tener Java 21 instalando, pueden utilizar https://sdkman.io/

**Linux:**
```
./gradlew run
```

**Windows:**
```
./gradlew.bat run
```

### Ejecutar desde Docker

Deben tener instalado Docker según su sistema operativo https://docs.docker.com/engine/install/

```
docker build -t prueba-mqtt . && docker run --rm -i prueba-mqtt
```