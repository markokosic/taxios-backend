# ==========================================
# Stage 1: Build the Java application
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom.xml and resolve dependencies (this layer is cached)
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy the source code and package the application (skipping tests)
COPY src ./src
RUN mvn package -DskipTests

# ==========================================
# Stage 2: Run the Java application
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the compiled JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot's default port
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
