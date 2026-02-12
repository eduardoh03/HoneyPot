# ===== Stage 1: Build =====
FROM eclipse-temurin:24-jdk-alpine AS build

RUN apk add --no-cache maven

WORKDIR /app

# Copiar pom.xml primeiro (para cachear dependências)
COPY pom.xml .

# Baixar dependências (camada cacheada enquanto pom.xml não mudar)
RUN mvn dependency:go-offline -B

# Copiar código-fonte e buildar
COPY src src
RUN mvn package -DskipTests -B

# ===== Stage 2: Runtime =====
FROM eclipse-temurin:24-jre-alpine

WORKDIR /app

COPY --from=build /app/target/HoneyPot-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
