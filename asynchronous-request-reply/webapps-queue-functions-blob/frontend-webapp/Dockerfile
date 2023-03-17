FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]