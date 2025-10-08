# ===== Build stage =====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src

# Wichtig: Tests NICHT kompilieren UND nicht ausführen weil gehen grad nicht wegen Kampf gegen rabbitMQ
# wenn tests wieder laufen, nächste Zeile wieder auf "RUN mvn -q -DskipTests package" ändern
RUN mvn -q -Dmaven.test.skip=true package
#            ^^^^^^^^^^^^^^^^^^^

# ===== Run stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8081
CMD ["java","-jar","app.jar"]