# Backend Dockerfile - multi-stage build
FROM maven:3.9-eclipse-temurin-17-noble AS build
WORKDIR /app

# Copy pom and wrapper (if present) first to leverage cache
COPY pom.xml mvnw* ./
COPY .mvn .mvn
COPY src ./src

# Package the application (skip tests during image build by default)
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the jar produced by the builder stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
