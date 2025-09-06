@echo off
echo Compilando proyecto Spring Boot...

set JAVA_HOME=D:\java-portable\jdk-17.0.2
set MAVEN_HOME=D:\maven-portable\apache-maven-3.9.4
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo Usando Java: %JAVA_HOME%
echo Usando Maven: %MAVEN_HOME%

"%JAVA_HOME%\bin\java.exe" -cp "%MAVEN_HOME%\boot\plexus-classworlds-2.7.0.jar" -Dclassworlds.conf="%MAVEN_HOME%\bin\m2.conf" -Dmaven.home="%MAVEN_HOME%" -Dlibrary.jansi.path="%MAVEN_HOME%\lib\jansi-native" -Dmaven.multiModuleProjectDirectory="%CD%" org.codehaus.plexus.classworlds.launcher.Launcher clean compile

pause
