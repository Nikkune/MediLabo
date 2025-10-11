# MediLabo — Documentation

Ce dépôt contient une architecture microservices pour l'application MediLabo :
- ms-eureka (Serveur Eureka)
- ms-gateway (API Gateway Spring Cloud)
- ms-patient (service Patients — Spring Boot)
- ms-notes (service Notes — Spring Boot + MongoDB)
- ms-risk (service Évaluation du Risque — Spring Boot)
- ms-front (Frontend React/Vite, servi par Nginx en prod)
- docker/mongo-init (fichiers d'initialisation MongoDB : data.json et init-mongo.sh)

## Sommaire
- [Aperçu des ports par défaut](#aperçu-des-ports-par-défaut)
- [Installation avec Docker (recommandé)](#installation-avec-docker-recommandé)
  - [Lancement en une commande (Docker Compose) (recommandé)](#lancement-en-une-commande-docker-compose-recommandé)
  - [Lancement sans Compose](#lancement-sans-compose)
- [Installation manuelle (sans Docker / sans Compose)](#installation-manuelle-sans-docker--sans-compose)
- [Green Code — pistes de mise en œuvre](#green-code--pistes-de-mise-en-œuvre)
- [Structure utile du dépôt](#structure-utile-du-dépôt)
- [Licence](#licence)

## Aperçu des ports par défaut

- ms-eureka : 8761 (tableau de bord Eureka)
- ms-gateway : 8080
- ms-patient : 8081
- ms-notes : 8082
- ms-risk : 8083
- ms-front (Nginx) : 80
- MongoDB : 27017

Ajustez au besoin si vos configurations d'application diffèrent.

## Installation avec Docker (recommandé)

### Lancement en une commande (Docker Compose) (recommandé)

**Important** : Cette section suppose que vous souhaitez tout lancer en conteneurs, **avec** `docker compose`.
 - Si vous voulez tout lancer **sans** Docker Compose, utilisez la section [« Lancement sans Compose »](#lancement-sans-compose).
 - Si vous voulez tout lancer sans Docker, utilisez la section [« Installation manuelle (sans Docker / sans Compose) »](#installation-manuelle-sans-docker--sans-compose).

#### Prérequis logiciels
- Docker / Docker Desktop récent
- Docker Compose
- PowerShell (Windows) ou un shell équivalent

Pour tout lancer automatiquement dans le bon ordre (bases de données, Eureka, microservices, frontend) et attendre la disponibilité des dépendances critiques, utilisez Docker Compose.

Commandes (à la racine du projet) :

Pour construire et lancer l'application :
```powershell
docker compose up -d --build
```

Pour suivre les logs si besion :
```powershell
docker compose logs -f --tail=100
```

Pour arrêter et supprimer les conteneurs de l'application :
```powershell
docker compose down
```

Notes :
- Le service MongoDB inclut un healthcheck et un seed automatique via docker/mongo-init (data.json + init-mongo.sh).
- MySQL est inclus pour ms-patient avec un healthcheck; les variables SPRING_DATASOURCE_* surchargent la configuration packagée.
- Les services Spring démarrent même si Eureka n’est pas encore prêt; ils se (ré)enregistreront automatiquement.
- Les ports exposés par défaut : 80 (front), 8080 (gateway), 8761 (Eureka), 8081/8082/8083 (services), 27017 (Mongo), 3306 (MySQL).

Les Dockerfiles sont déjà fournis pour chaque microservice. Un répertoire d'init MongoDB est disponible dans `docker/mongo-init/` contenant :
- `data.json` : données de seed pour la collection
- `init-mongo.sh` : script d'import automatique (utilise `mongoimport`)

Le script s'exécute automatiquement si vous montez le dossier sur `/docker-entrypoint-initdb.d` du conteneur MongoDB.

---

### Lancement sans Compose
**Important** : Cette section suppose que vous souhaitez tout lancer en conteneurs, **sans** `docker compose`.
 - Si vous voulez tout lancer **avec** Docker Compose, utilisez la section [« Lancement en une commande (Docker Compose) (recommandé) »](#lancement-en-une-commande-docker-compose-recommandé).
 - Si vous voulez tout lancer sans Docker, utilisez la section [« Installation manuelle (sans Docker / sans Compose) »](#installation-manuelle-sans-docker--sans-compose).

#### 1) Prérequis logiciels
- Docker Engine / Docker Desktop récent
- PowerShell (Windows) ou un shell équivalent

#### 2) Réseau et volumes Docker
Exécutez ces commandes une seule fois (elles sont idempotentes) :

```powershell
# Réseau dédié (pour que les conteneurs se résolvent par leurs noms)
if (-not (docker network ls --format '{{.Name}}' | Select-String -SimpleMatch 'medilabo-net')) { docker network create medilabo-net }

# Volumes de données persistantes
if (-not (docker volume ls --format '{{.Name}}' | Select-String -SimpleMatch 'mongo-data')) { docker volume create mongo-data }
if (-not (docker volume ls --format '{{.Name}}' | Select-String -SimpleMatch 'mysql-data')) { docker volume create mysql-data }
```

#### 3) Build des images
Depuis la racine du dépôt :

```powershell
# Backend Spring Boot
docker build -t medilabo/ms-eureka:local   ./ms-eureka
docker build -t medilabo/ms-gateway:local  ./ms-gateway
docker build -t medilabo/ms-patient:local  ./ms-patient
docker build -t medilabo/ms-notes:local    ./ms-notes
docker build -t medilabo/ms-risk:local     ./ms-risk

# Frontend (Nginx)
docker build -t medilabo/ms-front:local    ./ms-front
```

#### 4) Lancement des bases de données
Lancez d’abord MongoDB et MySQL pour que les microservices puissent s’y connecter.

```powershell
# MongoDB (avec seed automatique depuis docker/mongo-init)
docker run -d --name mongo --network medilabo-net -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=example -e MONGO_DB_NAME=notesdb -e MONGO_COLLECTION=notes -v "${PWD}.Path\docker\mongo-init:/docker-entrypoint-initdb.d:ro" -v mongo-data:/data/db mongo:7

# MySQL
docker run -d --name mysql --network medilabo-net -p 3306:3306 -e MYSQL_DATABASE=medilabo_data_store -e MYSQL_USER=medilabo -e MYSQL_PASSWORD=medilabo -e MYSQL_ROOT_PASSWORD=example -v mysql-data:/var/lib/mysql mysql:8.4
```

Astuce: patientez quelques secondes pour laisser MySQL/MongoDB démarrer complètement. Vous pouvez vérifier les logs :

```powershell
docker logs -f --tail=100 mysql
# ou
docker logs -f --tail=100 mongo
```

#### 5) Lancement des microservices
Dans l’ordre recommandé :

```powershell
# 1) Eureka
docker run -d --name ms-eureka --network medilabo-net -p 8761:8761 medilabo/ms-eureka:local

# 2) Gateway (après Eureka)
docker run -d --name ms-gateway --network medilabo-net -p 8080:8080 -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka -e SPRING_SECURITY_USER_NAME=medilabo -e SPRING_SECURITY_USER_PASSWORD=medilabo123 -e SPRING_SECURITY_USER_ROLES=USER medilabo/ms-gateway:local

# 3) Patient (après MySQL + Eureka)
docker run -d --name ms-patient --network medilabo-net -p 8081:8081 -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/medilabo_data_store -e SPRING_DATASOURCE_USERNAME=medilabo -e SPRING_DATASOURCE_PASSWORD=medilabo -e SPRING_SECURITY_USER_NAME=medilabo -e SPRING_SECURITY_USER_PASSWORD=medilabo123 -e SPRING_SECURITY_USER_ROLES=USER medilabo/ms-patient:local

# 4) Notes (après MongoDB + Eureka)
docker run -d --name ms-notes --network medilabo-net -p 8082:8082 -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka -e SPRING_DATA_MONGODB_HOST=mongo -e SPRING_DATA_MONGODB_PORT=27017 -e SPRING_DATA_MONGODB_DATABASE=notesdb -e SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=admin -e SPRING_DATA_MONGODB_USERNAME=root -e SPRING_DATA_MONGODB_PASSWORD=example -e SPRING_SECURITY_USER_NAME=medilabo -e SPRING_SECURITY_USER_PASSWORD=medilabo123 -e SPRING_SECURITY_USER_ROLES=USER -e MS_PATIENT_BASEURL=http://ms-gateway:8080/patient -e MS_PATIENT_USERNAME=medilabo -e MS_PATIENT_PASSWORD=medilabo123 medilabo/ms-notes:local

# 5) Risk (après Gateway)
docker run -d --name ms-risk --network medilabo-net -p 8083:8083 -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka -e SPRING_SECURITY_USER_NAME=medilabo -e SPRING_SECURITY_USER_PASSWORD=medilabo123 -e SPRING_SECURITY_USER_ROLES=USER -e MS_PATIENT_BASEURL=http://ms-gateway:8080/patient -e MS_PATIENT_USERNAME=medilabo -e MS_PATIENT_PASSWORD=medilabo123 -e MS_NOTES_BASEURL=http://ms-gateway:8080/notes -e MS_NOTES_USERNAME=medilabo -e MS_NOTES_PASSWORD=medilabo123 medilabo/ms-risk:local
```

#### 6) Lancement du frontend (Nginx)
Après que la gateway soit en ligne :

```powershell
docker run -d --name ms-front --network medilabo-net -p 80:80 medilabo/ms-front:local
```

#### 7) Vérification
- Eureka : http://localhost:8761 doit afficher les services.
- API Gateway : http://localhost:8080/patient et http://localhost:8080/notes
- Frontend : http://localhost/ (selon votre config Nginx et routes)
- Logs : `docker logs -f <container>` pour diagnostiquer un service.

#### 8) Clean-up
Arrêtez et supprimez tous les conteneurs, conservez les volumes (ou pas) :

```powershell
# Arrêt
docker stop ms-front ms-risk ms-notes ms-patient ms-gateway ms-eureka mysql mongo

# Suppression
docker rm   ms-front ms-risk ms-notes ms-patient ms-gateway ms-eureka mysql mongo

# (Optionnel) supprimer les volumes de données
docker volume rm mongo-data mysql-data

# (Optionnel) supprimer le réseau dédié
docker network rm medilabo-net
```

## Installation manuelle (sans Docker / sans Compose)

Ce mode est utile pour le développement local, le débogage fin ou lorsque Docker n’est pas disponible. Les étapes ci‑dessous reprennent la logique du docker-compose (mêmes ports, même nom de base de données) afin de rester cohérent.

**Important** : Cette section suppose que vous souhaitez tout lancer **sans** `docker`.
- Si vous voulez tout lancer **avec** Docker Compose, utilisez la section [« Lancement en une commande (Docker Compose) (recommandé) »](#lancement-en-une-commande-docker-compose-recommandé).
- Si vous voulez tout lancer **sans** Docker Compose, utilisez la section [« Lancement sans Compose »](#lancement-sans-compose).

**Important** : vous devez créer des fichiers database.properties pour ms-notes et ms-patient [(voir étape 2.2)](#22-fichiers-databaseproperties-à-créer).

### 1) Prérequis logiciels
- Java 21 + Maven 3.9+
- Node.js 22.x + npm
- MongoDB 6.x+ installé localement OU un accès à une instance distante
- MySQL 8.x installé localement OU un accès à une instance distante
- Accès réseau aux ports locaux suivants par défaut: 8761, 8080, 8081, 8082, 8083

### 2) Installations et configuration des bases de données

#### 2.1) Création et initialisation
- MySQL
  1. Démarrez MySQL et connectez‑vous comme administrateur.
  2. Créez une base et (optionnellement) un utilisateur dédié:
     ```sql
     CREATE DATABASE medilabo_data_store CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
     CREATE USER 'medilabo'@'%' IDENTIFIED BY 'mot_de_passe_fort';
     GRANT ALL PRIVILEGES ON medilabo_data_store.* TO 'medilabo'@'%';
     FLUSH PRIVILEGES;
     ```
  3. Note: ms-patient peut créer le schéma au démarrage (ddl-auto create-drop en dev).

- MongoDB
  1. Démarrez MongoDB et assurez‑vous que l’authentification est configurée selon votre besoin.
  2. Création de la base et d’un utilisateur avec mongosh :
     - Connexion (sans authentification) :
       ```bash
       mongosh --host <host> --port <port>
       ```
     - Connexion (avec authentification) :
       ```bash
       mongosh --host <host> --port <port> -u <admin_user> -p <admin_password> --authenticationDatabase admin
       ```
     - Dans le shell mongosh, exécutez :
       ```mongosh
       // Selectionne ou créer une table
       use medilabo_data_store;
       
       // Créer la collection
       db.createCollection("notes");
       
       // Créer un utilisateur applicatif avec droits readWrite sur la base
       db.createUser({
         user: "medilabo",
         pwd: "mot_de_passe_fort",
         roles: [ { role: "readWrite", db: "medilabo_data_store" } ]
       });
       ```
  3. (Optionnel) Import de données d’exemple avec mongoimport (reprend la logique du dossier docker/mongo-init/) :
     ```bash
     mongoimport --host <host> --port <port> --username <user> --password <pass> --authenticationDatabase <authDb> --db medilabo_data_store --collection notes --jsonArray --file docker/mongo-init/data.json
     ```

#### 2.2) Fichiers database.properties à créer
Créez les fichiers suivants avec ces contenus exacts, puis renseignez les valeurs selon votre environnement.

- ms-notes/src/main/resources/database.properties

  Contenu:
    ```properties
    # MongoDB host/port
    spring.data.mongodb.host=
    spring.data.mongodb.port=
    # Base de données principale
    spring.data.mongodb.database=
    # Authentification
    spring.data.mongodb.username=
    spring.data.mongodb.password=
    spring.data.mongodb.authentication-database=
    ```

- ms-patient/src/main/resources/database.properties

  Contenu:
    ```properties
    spring.datasource.url=jdbc:mysql://host:port/database
    spring.datasource.username=
    spring.datasource.password=
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    spring.jpa.show-sql=true
    spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
    ```

Remarques:
- Ces fichiers sont importés par Spring via spring.config.import défini dans application.properties de chaque service.
- Ne versionnez pas des mots de passe réels. Utilisez des variables d’environnement/secrets si nécessaire.

### 3) Lancement des services (ordre recommandé)
Dans des terminaux séparés, à la racine de chaque microservice:

1. ms-eureka
   ```bash
   mvn -q -DskipTests package
   java -jar target/*.jar
   ```
   - Vérifiez http://localhost:8761

2. ms-patient (dépend de MySQL)
   - Vérifiez ms-patient/src/main/resources/database.properties
   ```bash
   mvn -q -DskipTests spring-boot:run
   ```
   - Le service écoute sur http://localhost:8081

3. ms-notes (dépend de MongoDB et de ms-patient/gateway pour certains appels)
   - Vérifiez ms-notes/src/main/resources/database.properties
   ```bash
   mvn -q -DskipTests spring-boot:run
   ```
   - Le service écoute sur http://localhost:8082

4. ms-risk
   ```bash
   mvn -q -DskipTests spring-boot:run
   ```
   - Écoute sur http://localhost:8083

5. ms-gateway (dépend d’Eureka et des services)
   ```bash
   mvn -q -DskipTests spring-boot:run
   ```
   - API Gateway sur http://localhost:8080

Astuce: vous pouvez aussi builder une fois tous les JARs avec mvn -q -DskipTests package à la racine de chaque module puis lancer avec java -jar target/xxx.jar.

### 4) Lancement du frontend
- Aller dans ms-front
- npm install
- npm run dev
- Ouvrir l’URL de développement affichée (souvent http://localhost:5173). La configuration de proxy Vite doit pointer vers la gateway (http://localhost:8080) si utilisée.

### 5) Vérification
- Eureka: http://localhost:8761 liste les services enregistrés.
- API Gateway: http://localhost:8080/patient et /notes doivent répondre (selon vos routes configurées).
- Logs: surveillez les terminaux pour d’éventuelles erreurs de connexion à MySQL/MongoDB.

### 6) Clean‑up
- Arrêtez les processus (Ctrl+C) dans chaque terminal.
- Facultatif: supprimez/arrêtez vos services MySQL/MongoDB locaux si lancés pour les tests.

## Green Code — pistes de mise en œuvre

Objectif: aller à l’essentiel pour réduire CPU/RAM/IO et trafic, sans complexifier le projet. Appliquez en priorité ces quelques actions simples:

- Docker: images minces (JRE/distroless) et builds multi‑étapes; nettoyez les caches de build.
- JVM/Spring: limitez la mémoire (MaxRAMPercentage), logs en INFO, compression HTTP activée sur la Gateway.
- Base de données: index sur les champs de recherche clés; charger uniquement ce qui est nécessaire (projections/DTO).
- API: toujours paginer les listes et limiter la taille des réponses par défaut.
- Frontend: build de prod, assets minifiés et compressés, cache long pour les fichiers versionnés.
- Nginx: gzip activé; logs d’accès désactivés en prod si non utiles.
- CI/CD: cache Maven/npm; utilisez Docker BuildKit.
- Opérations: arrêtez les conteneurs non utilisés; surveillez erreurs 5xx et pics CPU.

Checklist rapide
- [ ] Logs en INFO, compression HTTP active
- [ ] Pagination en place sur les listes
- [ ] Index DB clés présents
- [ ] Front en build prod + cache/Compression
- [ ] Images Docker minces

## Structure utile du dépôt

- `ms-*/Dockerfile` : images des microservices
- `ms-front/nginx.conf` : config Nginx du frontend
- `docker/mongo-init/` : seed MongoDB (`data.json`) et script `init-mongo.sh`

## Authors

- [@Nikkune](https://www.github.com/Nikkune)