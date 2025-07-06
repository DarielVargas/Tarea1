## Proyecto Demo MQTT

Aplicación en Java que se suscribe a un broker MQTT y almacena en una base de
datos las mediciones enviadas desde un sensor BM280 conectado a un ESP32.
Anteriormente esta aplicación generaba datos simulados, pero ahora únicamente
consume los valores recibidos.

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