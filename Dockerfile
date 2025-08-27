FROM eclipse-temurin:24-jdk-alpine
LABEL authors="eduardoh03"

WORKDIR /app

ARG JAR_FILE=target/HoneyPot-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
