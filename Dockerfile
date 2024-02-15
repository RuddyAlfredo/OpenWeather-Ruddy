#DOCKERFILE EN FORMATO UBER JAR ESTANDAR PARA UN CONTENEDOR DOCKER SIMPLE

#Imagen de java para el jdk 17
FROM eclipse-temurin:17-jdk
#Volumen tempooral para la copia y construccion de la imagen en memoria (recomendado en Spring)
VOLUME /tmp   
#Copiando una imagen simple solamente con el fat jar(denttro van las depend., tomcat y código fuente) y asignandole el nombre de "app.jar"
COPY target/*.jar app.jar
#Punto de entrada (como va a arrancar el contenedor)
ENTRYPOINT [ "java", "-jar", "app.jar" ]
