# Basis-Image: JDK 17
FROM openjdk:17-jdk-slim

# Arbeitsverzeichnis im Container
WORKDIR /app

# Nimm das eine JAR aus target/ (Build muss vorher lokal gelaufen sein)
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose Port 8081 für den Container
EXPOSE 8081

# Startbefehl für das JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
