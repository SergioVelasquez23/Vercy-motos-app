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

# Usar imagen más liviana para ejecutar
FROM openjdk:17-jre-slim

# Copiar el JAR compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE $PORT

# Comando para ejecutar la aplicación
CMD java -Dserver.port=$PORT -jar app.jar
