
# =====================================
# STAGE 1: BUILD APPLICATION
# =====================================

FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven configuration
COPY pom.xml .

# Copy source code
COPY src ./src

# Build Spring Boot JAR
RUN mvn clean package -DskipTests


# =====================================
# STAGE 2: RUN APPLICATION
# =====================================

FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy generated JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Render application port
EXPOSE 10000

# Start Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
