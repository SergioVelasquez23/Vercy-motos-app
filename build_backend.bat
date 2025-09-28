@echo off
echo "=== Compilando Backend Spring Boot ==="
echo.

REM Verificar Java
echo "Verificando Java..."
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java no está instalado o configurado correctamente
    echo Instala Java 17+ y configura JAVA_HOME
    pause
    exit /b 1
)

REM Limpiar build anterior
echo "Limpiando builds anteriores..."
if exist target rmdir /s /q target

REM Compilar con Maven Wrapper (preferido)
echo "Compilando con Maven..."
if exist mvnw.cmd (
    echo "Usando Maven Wrapper..."
    mvnw.cmd clean package -DskipTests
) else (
    echo "Usando Maven del sistema..."
    mvn clean package -DskipTests
)

if %errorlevel% equ 0 (
    echo.
    echo "=== COMPILACIÓN EXITOSA ==="
    echo "JAR generado en: target/"
    dir target\*.jar
    echo.
    echo "Para ejecutar el backend:"
    echo "java -jar target\[nombre-del-jar].jar"
) else (
    echo.
    echo "=== ERROR EN COMPILACIÓN ==="
)

pause
