# Usar imagen base de OpenJDK 17
FROM openjdk:17-jdk-slim

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Dar permisos de ejecución a mvnw
RUN chmod +x mvnw

# Descargar dependencias (para cache de Docker)
RUN ./mvnw dependency:resolve

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Exponer puerto
EXPOSE $PORT

# Comando para ejecutar la aplicación
CMD java -Dserver.port=$PORT -jar target/*.jar
