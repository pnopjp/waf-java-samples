FROM mcr.microsoft.com/openjdk/jdk:11-ubuntu
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV spring_profiles_active docker
ENTRYPOINT ["java","-jar","/app.jar"]