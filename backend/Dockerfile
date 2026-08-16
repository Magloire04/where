# --- Étape build : compile le jar Spring Boot ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -B -DskipTests package

# --- Étape run : image légère avec le JRE 21 ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/orientation-backend-0.1.0.jar app.jar
# Le PaaS fournit $PORT ; application.yml lit server.port=${PORT:8080}.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
