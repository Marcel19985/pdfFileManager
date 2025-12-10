# --- Build stage ---
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# pom vorab kopieren, Dependencies cachen
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# dann den Source-Code kopieren
COPY src ./src

# Jar bauen
RUN mvn -q -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# fertiges Jar aus dem Build-Container kopieren
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
