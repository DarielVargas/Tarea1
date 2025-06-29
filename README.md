## Proyecto Demo MQTT

Demostración sobre el uso MQTT utilizando Java, 
creando Publicadores y Subcriptores mediante el servidor externo de
Mosquitto.

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

### Tabla de estaciones

Al recibir datos de los sensores se registra automáticamente la estación
correspondiente en la tabla `estaciones`. Cada registro almacena:

- `id`: identificador publicado en el tópico (por ejemplo `estacion-1`).
- `nombre`: nombre descriptivo de la estación.
- `ubicacion`: texto con la ubicación de la estación.
