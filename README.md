# MediLabo — Documentation

This repository contains a microservices architecture for the MediLabo application:
- ms-eureka (Eureka Server)
- ms-gateway (Spring Cloud API Gateway)
- ms-patient (Patients service — Spring Boot)
- ms-notes (Notes service — Spring Boot + MongoDB)
- ms-risk (Risk Assessment service — Spring Boot)
- ms-front (React/Vite frontend, served by Nginx in production)
- docker/mongo-init (MongoDB initialization files: data.json and init-mongo.sh)

## Table of Contents
- [Default Ports Overview](#default-ports-overview)
- [Installation with Docker (recommended)](#installation-with-docker-recommended)
  - [One-command startup (Docker Compose) (recommended)](#one-command-startup-docker-compose-recommended)
  - [Startup without Compose](#startup-without-compose)
- [Manual Installation (without Docker / without Compose)](#manual-installation-without-docker--without-compose)
- [Green Code — implementation hints](#green-code--implementation-hints)
- [Useful repository structure](#useful-repository-structure)
- [Authors](#authors)

## Default Ports Overview

- ms-eureka: 8761 (Eureka dashboard)
- ms-gateway: 8080
- ms-patient: 8081
- ms-notes: 8082
- ms-risk: 8083
- ms-front (Nginx): 80
- MongoDB: 27017

Adjust as needed if your application configuration differs.

## Installation with Docker (recommended)

### One-command startup (Docker Compose) (recommended)

Important: This section assumes you want to run everything in containers using docker compose.
 - If you want to run everything without Docker Compose, use the section “Startup without Compose”.
 - If you want to run without Docker at all, use the section “Manual Installation (without Docker / without Compose)”.

#### Software prerequisites
- Recent Docker / Docker Desktop
- Docker Compose
- PowerShell (Windows) or an equivalent shell

To start everything automatically in the right order (databases, Eureka, microservices, frontend) and wait for critical dependencies to be available, use Docker Compose.

Commands (from the project root):

To build and start the application:
```powershell
docker compose up -d --build
```

To follow logs if needed:
```powershell
docker compose logs -f --tail=100
```

To stop and remove the application containers:
```powershell
docker compose down
```

Notes:
- The MongoDB service includes a healthcheck and automatic seeding via docker/mongo-init (data.json + init-mongo.sh).
- MySQL is included for ms-patient with a healthcheck; SPRING_DATASOURCE_* environment variables override packaged config.
- Spring services start even if Eureka is not yet ready; they will (re)register automatically.
- Default exposed ports: 80 (front), 8080 (gateway), 8761 (Eureka), 8081/8082/8083 (services), 27017 (Mongo), 3306 (MySQL).

Dockerfiles are already provided for each microservice. A MongoDB init directory is available in `docker/mongo-init/` containing:
- `data.json`: seed data for the collection
- `init-mongo.sh`: automatic import script (uses `mongoimport`)

The script runs automatically if you mount the folder to `/docker-entrypoint-initdb.d` of the MongoDB container.

---

### Startup without Compose
Important: This section assumes you want to run everything in containers without docker compose.
 - If you want to run everything with Docker Compose, use the section “One-command startup (Docker Compose) (recommended)”.
 - If you want to run without Docker, use the section “Manual Installation (without Docker / without Compose)”.

#### 1) Software prerequisites
- Docker Engine / Docker Desktop (recent)
- PowerShell (Windows) or an equivalent shell

#### 2) Docker network and volumes
Run these commands once (they are idempotent):

```powershell
# Dedicated network (so containers can resolve each other by name)
if (-not (docker network ls --format '{{.Name}}' | Select-String -SimpleMatch 'medilabo-net')) { docker network create medilabo-net }

# Persistent data volumes
if (-not (docker volume ls --format '{{.Name}}' | Select-String -SimpleMatch 'mongo-data')) { docker volume create mongo-data }
if (-not (docker volume ls --format '{{.Name}}' | Select-String -SimpleMatch 'mysql-data')) { docker volume create mysql-data }
```

#### 3) Build images
From the repository root:

```powershell
# Backend (Spring Boot)
docker build -t medilabo/ms-eureka:local   ./ms-eureka
docker build -t medilabo/ms-gateway:local  ./ms-gateway
docker build -t medilabo/ms-patient:local  ./ms-patient
docker build -t medilabo/ms-notes:local    ./ms-notes
docker build -t medilabo/ms-risk:local     ./ms-risk

# Frontend (Nginx)
docker build -t medilabo/ms-front:local    ./ms-front
```

#### 4) Start the databases
Start MongoDB and MySQL first so microservices can connect to them.

```powershell
# MongoDB (with automatic seeding from docker/mongo-init)
docker run -d --name mongo --network medilabo-net -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=example -e MONGO_DB_NAME=notesdb -e MONGO_COLLECTION=notes -v "${PWD}.Path\docker\mongo-init:/docker-entrypoint-initdb.d:ro" -v mongo-data:/data/db mongo:7

# MySQL
docker run -d --name mysql --network medilabo-net -p 3306:3306 -e MYSQL_DATABASE=medilabo_data_store -e MYSQL_USER=medilabo -e MYSQL_PASSWORD=medilabo -e MYSQL_ROOT_PASSWORD=example -v mysql-data:/var/lib/mysql mysql:8.4
```

Tip: wait a few seconds to let MySQL/MongoDB fully start. You can check logs:

```powershell
docker logs -f --tail=100 mysql
# or
docker logs -f --tail=100 mongo
```

#### 5) Start the microservices
In the recommended order:

```powershell
# 1) Eureka
docker run -d --name ms-eureka --network medilabo-net -p 8761:8761 medilabo/ms-eureka:local

# 2) Gateway (after Eureka)
docker run -d --name ms-gateway --network medilabo-net -p 8080:8080 -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka -e SPRING_SECURITY_USER_NAME=medilabo -e SPRING_SECURITY_USER_PASSWORD=medilabo123 -e SPRING_SECURITY_USER_ROLES=USER medilabo/ms-gateway:local

# 3) Patient (after MySQL + Eureka)
docker run -d --name ms-patient --network medilabo-net -p 8081:8081 -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/medilabo_data_store -e SPRING_DATASOURCE_USERNAME=medilabo -e SPRING_DATASOURCE_PASSWORD=medilabo -e SPRING_SECURITY_USER_NAME=medilabo -e SPRING_SECURITY_USER_PASSWORD=medilabo123 -e SPRING_SECURITY_USER_ROLES=USER medilabo/ms-patient:local

# 4) Notes (after MongoDB + Eureka)
docker run -d --name ms-notes --network medilabo-net -p 8082:8082 -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka -e SPRING_DATA_MONGODB_HOST=mongo -e SPRING_DATA_MONGODB_PORT=27017 -e SPRING_DATA_MONGODB_DATABASE=notesdb -e SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=admin -e SPRING_DATA_MONGODB_USERNAME=root -e SPRING_DATA_MONGODB_PASSWORD=example -e SPRING_SECURITY_USER_NAME=medilabo -e SPRING_SECURITY_USER_PASSWORD=medilabo123 -e SPRING_SECURITY_USER_ROLES=USER -e MS_PATIENT_BASEURL=http://ms-gateway:8080/patient -e MS_PATIENT_USERNAME=medilabo -e MS_PATIENT_PASSWORD=medilabo123 medilabo/ms-notes:local

# 5) Risk (after Gateway)
docker run -d --name ms-risk --network medilabo-net -p 8083:8083 -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka -e SPRING_SECURITY_USER_NAME=medilabo -e SPRING_SECURITY_USER_PASSWORD=medilabo123 -e SPRING_SECURITY_USER_ROLES=USER -e MS_PATIENT_BASEURL=http://ms-gateway:8080/patient -e MS_PATIENT_USERNAME=medilabo -e MS_PATIENT_PASSWORD=medilabo123 -e MS_NOTES_BASEURL=http://ms-gateway:8080/notes -e MS_NOTES_USERNAME=medilabo -e MS_NOTES_PASSWORD=medilabo123 medilabo/ms-risk:local
```

#### 6) Start the frontend (Nginx)
After the gateway is online:

```powershell
docker run -d --name ms-front --network medilabo-net -p 80:80 medilabo/ms-front:local
```

#### 7) Verification
- Eureka: http://localhost:8761 should list the services.
- API Gateway: http://localhost:8080/patient and http://localhost:8080/notes
- Frontend: http://localhost/ (depending on your Nginx config and routes)
- Logs: `docker logs -f <container>` to diagnose a service.

#### 8) Clean-up
Stop and remove all containers; keep volumes (or not):

```powershell
# Stop
docker stop ms-front ms-risk ms-notes ms-patient ms-gateway ms-eureka mysql mongo

# Remove
docker rm   ms-front ms-risk ms-notes ms-patient ms-gateway ms-eureka mysql mongo

# (Optional) remove data volumes
docker volume rm mongo-data mysql-data

# (Optional) remove the dedicated network
docker network rm medilabo-net
```

## Manual Installation (without Docker / without Compose)

This mode is useful for local development, fine-grained debugging, or when Docker is not available. The steps below mirror docker-compose logic (same ports, same database name) to stay consistent.

Important: This section assumes you want to run everything without docker.
- If you want to run everything with Docker Compose, use the section “One-command startup (Docker Compose) (recommended)”.
- If you want to run everything without Docker Compose, use the section “Startup without Compose”.

Important: you must create database.properties files for ms-notes and ms-patient (see step 2.2).

### 1) Software prerequisites
- Java 21 + Maven 3.9+
- Node.js 22.x + npm
- MongoDB 6.x+ installed locally OR access to a remote instance
- MySQL 8.x installed locally OR access to a remote instance
- Network access to the following local ports by default: 8761, 8080, 8081, 8082, 8083

### 2) Database setup and configuration

#### 2.1) Creation and initialization
- MySQL
  1. Start MySQL and connect as administrator.
  2. Create a database and (optionally) a dedicated user:
     ```sql
     CREATE DATABASE medilabo_data_store CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
     CREATE USER 'medilabo'@'%' IDENTIFIED BY 'strong_password';
     GRANT ALL PRIVILEGES ON medilabo_data_store.* TO 'medilabo'@'%';
     FLUSH PRIVILEGES;
     ```
  3. Note: ms-patient can create the schema at startup (ddl-auto create-drop in dev).

- MongoDB
  1. Start MongoDB and ensure authentication is configured as needed.
  2. Create the database and a user with mongosh:
     - Connect (without authentication):
       ```bash
       mongosh --host <host> --port <port>
       ```
     - Connect (with authentication):
       ```bash
       mongosh --host <host> --port <port> -u <admin_user> -p <admin_password> --authenticationDatabase admin
       ```
     - In the mongosh shell, run:
       ```mongosh
       // Select or create the database
       use medilabo_data_store;
       
       // Create the collection
       db.createCollection("notes");
       
       // Create an application user with readWrite on the DB
       db.createUser({
         user: "medilabo",
         pwd: "strong_password",
         roles: [ { role: "readWrite", db: "medilabo_data_store" } ]
       });
       ```
  3. (Optional) Import sample data with mongoimport (mirrors docker/mongo-init/ logic):
     ```bash
     mongoimport --host <host> --port <port> --username <user> --password <pass> --authenticationDatabase <authDb> --db medilabo_data_store --collection notes --jsonArray --file docker/mongo-init/data.json
     ```

#### 2.2) database.properties files to create
Create the following files with these exact keys, then fill values for your environment.

- ms-notes/src/main/resources/database.properties

  Content:
    ```properties
    # MongoDB host/port
    spring.data.mongodb.host=
    spring.data.mongodb.port=
    # Main database
    spring.data.mongodb.database=
    # Authentication
    spring.data.mongodb.username=
    spring.data.mongodb.password=
    spring.data.mongodb.authentication-database=
    ```

- ms-patient/src/main/resources/database.properties

  Content:
    ```properties
    spring.datasource.url=jdbc:mysql://host:port/database
    spring.datasource.username=
    spring.datasource.password=
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    spring.jpa.show-sql=true
    spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
    ```

Notes:
- These files are imported by Spring via spring.config.import defined in each service’s application.properties.
- Do not commit real passwords. Use environment variables/secrets if needed.

### 3) Start the services (recommended order)
In separate terminals, at each microservice root:

1. ms-eureka
   ```bash
   mvn -q -DskipTests package
   java -jar target/*.jar
   ```
   - Check http://localhost:8761

2. ms-patient (depends on MySQL)
   - Check ms-patient/src/main/resources/database.properties
   ```bash
   mvn -q -DskipTests spring-boot:run
   ```
   - Service listens on http://localhost:8081

3. ms-notes (depends on MongoDB and on ms-patient/gateway for some calls)
   - Check ms-notes/src/main/resources/database.properties
   ```bash
   mvn -q -DskipTests spring-boot:run
   ```
   - Service listens on http://localhost:8082

4. ms-risk
   ```bash
   mvn -q -DskipTests spring-boot:run
   ```
   - Listens on http://localhost:8083

5. ms-gateway (depends on Eureka and services)
   ```bash
   mvn -q -DskipTests spring-boot:run
   ```
   - API Gateway on http://localhost:8080

Tip: you can also build all JARs once with mvn -q -DskipTests package at each module root then run with java -jar target/xxx.jar.

### 4) Start the frontend
- Go to ms-front
- npm install
- npm run dev
- Open the displayed dev URL (often http://localhost:5173). Vite proxy config should point to the gateway (http://localhost:8080) if used.

### 5) Verification
- Eureka: http://localhost:8761 lists registered services.
- API Gateway: http://localhost:8080/patient and /notes should respond (depending on your configured routes).
- Logs: watch terminals for potential MySQL/MongoDB connection errors.

### 6) Clean‑up
- Stop the processes (Ctrl+C) in each terminal.
- Optional: remove/stop your local MySQL/MongoDB services if started for tests.

## Green Code — implementation hints

Goal: focus on the essentials to reduce CPU/RAM/IO and network traffic without complicating the project. Prioritize these few simple actions:

- Docker: slim images (JRE/distroless) and multi-stage builds; clean build caches.
- JVM/Spring: limit memory (MaxRAMPercentage), logs at INFO, enable HTTP compression on the Gateway.
- Database: indexes on key search fields; load only what’s needed (projections/DTO).
- API: always paginate lists and limit default response size.
- Frontend: production build, minified and compressed assets, long cache for versioned files.
- Nginx: enable gzip; disable access logs in prod if not useful.
- CI/CD: Maven/npm cache; use Docker BuildKit.
- Operations: stop unused containers; monitor 5xx errors and CPU spikes.

Quick checklist
- [ ] Logs at INFO, HTTP compression enabled
- [ ] Pagination in place on lists
- [ ] Key DB indexes present
- [ ] Front built for prod + cache/compression
- [ ] Slim Docker images

## Useful repository structure

- `ms-*/Dockerfile`: microservice images
- `ms-front/nginx.conf`: frontend Nginx config
- `docker/mongo-init/`: MongoDB seed (`data.json`) and `init-mongo.sh` script

## Authors

- [@Nikkune](https://www.github.com/Nikkune)