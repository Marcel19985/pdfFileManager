# Basis-Image: JDK 17
FROM openjdk:17-jdk-slim

# Arbeitsverzeichnis im Container
WORKDIR /app

# Kopiere das gebaute Spring-Boot-JAR ins Arbeitsverzeichnis
COPY target/swen3-teamM-0.0.1-SNAPSHOT.jar app.jar

# Expose Port 8081 für den Container
EXPOSE 8081

# Startbefehl für das JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
