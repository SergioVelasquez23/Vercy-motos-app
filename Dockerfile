# Usar imagen base con Maven incluido
FROM maven:3.8.4-openjdk-17 as build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (para cache de Docker)
RUN mvn dependency:resolve

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Usar imagen más liviana para ejecutar (Debian-based para mejor soporte SSL)
FROM eclipse-temurin:17-jre-jammy

# Instalar certificados CA y herramientas de red
RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates openssl && \
    update-ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Copiar el JAR compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE $PORT

# Comando para ejecutar la aplicación con configuración SSL mejorada
CMD java -Dserver.port=$PORT \
    -Djdk.tls.client.protocols=TLSv1.2 \
    -Dhttps.protocols=TLSv1.2 \
    -Djavax.net.ssl.trustStoreType=jks \
    -jar app.jar
