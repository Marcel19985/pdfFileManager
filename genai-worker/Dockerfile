# --- Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src

# --- Build stage
RUN mvn -q -DskipTests package \
 && echo "=== JAR CONTENTS (logger probe) ===" \
 && jar tf target/app.jar | grep -Ei 'StaticLoggerBinder|slf4j|logback|simple' || true \
 && echo "=== END PROBE ==="

# Build and rename the runnable jar to a stable name
RUN mvn -q -DskipTests package \
 && ls -lah target \
 && JAR=$(ls target/*.jar | grep -v 'original-') \
 && mv "$JAR" target/app.jar

# --- Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/app.jar /app/app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["java","-jar","/app/app.jar"]
# (If you need $JAVA_OPTS expansion, use: ["sh","-lc","exec java $JAVA_OPTS -jar /app/app.jar"])
