# ---- Stage 1: 构建前端 ----
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Stage 2: 构建后端，并把前端产物打进静态资源目录 ----
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn -B dependency:go-offline || true
COPY backend/src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn -B clean package -DskipTests

# ---- Stage 3: 运行时 ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
