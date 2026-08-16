# Conteneur unique (Spaceship Hyperlift) : le backend Spring Boot sert aussi le frontend statique.

# --- Étape 1 : build du frontend (SPA) ---
FROM node:20 AS frontend
WORKDIR /fe
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --legacy-peer-deps
COPY frontend/ ./
# Utilise .env.production (VITE_API_BASE_URL=/api/v1, même origine).
RUN npm run build

# --- Étape 2 : build du backend, frontend embarqué dans les ressources statiques ---
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
COPY --from=frontend /fe/dist/ ./src/main/resources/static/
RUN mvn -q -B -DskipTests package

# --- Étape 3 : image de run (JRE 21) ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/target/orientation-backend-0.1.0.jar app.jar
# Hyperlift fournit $PORT ; application.yml lit server.port=${PORT:8080}.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
