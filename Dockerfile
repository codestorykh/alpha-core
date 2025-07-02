
FROM maven:3.8.3-openjdk-17 AS build
MAINTAINER codestorykh
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8080