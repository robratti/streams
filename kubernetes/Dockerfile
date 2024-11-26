# Stage 1: Build the application
FROM openjdk:21-ea-1-jdk-slim AS builder

# Update and Install Maven
RUN apt-get update
RUN apt-get install -y maven

WORKDIR /app

# Copy application files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM ubuntu:22.04

RUN apt update
RUN apt install software-properties-common wget -y

RUN add-apt-repository ppa:openjdk-r/ppa -y
RUN apt update
RUN apt install openjdk-21-jre -y

WORKDIR /app

# Add non-root user for security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=local

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
