# syntax=docker/dockerfile:1

# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar
# Common Spring Cloud Gateway port
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
