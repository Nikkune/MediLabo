# MediLabo — Guide d'installation (Docker et Manuel)

Ce dépôt contient une architecture microservices pour l'application MediLabo :
- ms-eureka (Serveur Eureka)
- ms-gateway (API Gateway Spring Cloud)
- ms-patient (service Patients — Spring Boot)
- ms-notes (service Notes — Spring Boot + MongoDB)
- ms-risk (service Évaluation du Risque — Spring Boot)
- ms-front (Frontend React/Vite, servi par Nginx en prod)
- docker/mongo-init (fichiers d'initialisation MongoDB : data.json et init-mongo.sh)

Les instructions ci‑dessous détaillent l'installation via Docker (recommandée) et une exécution manuelle (sans Docker) pour le développement local.


## Prérequis

- Docker Desktop 4.x ou Docker Engine récent
- (Optionnel) Docker Compose v2
- Java 21 (Temurin/Adoptium) et Maven 3.9+ si vous lancez les services manuellement
- Node.js 22.x et npm si vous lancez le frontend manuellement
- Accès réseau aux ports utilisés : 80, 8080–8083, 8761 (par défaut)


## Aperçu des ports par défaut

- ms-eureka : 8761 (tableau de bord Eureka)
- ms-gateway : 8080
- ms-patient : 8081
- ms-notes : 8082
- ms-risk : 8083
- ms-front (Nginx) : 80
- MongoDB : 27017

Ajustez au besoin si vos configurations d'application diffèrent.


## Installation avec Docker (recommandé)

### Lancement en une commande (Docker Compose) (recommandé)

Pour tout lancer automatiquement dans le bon ordre (bases de données, Eureka, microservices, frontend) et attendre la disponibilité des dépendances critiques, utilisez Docker Compose.

Commandes principales (à la racine du projet) :

```powershell
# Build + run en arrière-plan
docker compose up -d --build

# Suivre les logs si besoin
docker compose logs -f --tail=100

# Arrêter et supprimer les conteneurs du compose
docker compose down
```

Notes :
- Le service MongoDB inclut un healthcheck et un seed automatique via docker/mongo-init (data.json + init-mongo.sh).
- MySQL est inclus pour ms-patient avec un healthcheck; les variables SPRING_DATASOURCE_* surchargent la configuration packagée.
- Les services Spring démarrent même si Eureka n’est pas encore prêt; ils se (ré)enregistreront automatiquement.
- Les ports exposés par défaut : 80 (front), 8080 (gateway), 8761 (Eureka), 8081/8082/8083 (services), 27017 (Mongo), 3306 (MySQL).

Les Dockerfiles sont déjà fournis pour chaque microservice. Un répertoire d'init MongoDB est disponible dans `docker/mongo-init/` contenant :
- `data.json` : données de seed pour la collection
- `init-mongo.sh` : script d'import automatique (utilise `mongoimport`)

Le script s'exécute automatiquement si vous montez le dossier sur `/docker-entrypoint-initdb.d` du conteneur MongoDB.


### Manuellement (sans Docker Compose)
#### 1) Construire les images

Exécutez depuis la racine du projet (PowerShell ou bash) :

```powershell
# À la racine du repo
docker build -t medilabo/ms-eureka .\ms-eureka
docker build -t medilabo/ms-gateway .\ms-gateway
docker build -t medilabo/ms-patient .\ms-patient
docker build -t medilabo/ms-notes .\ms-notes
docker build -t medilabo/ms-risk .\ms-risk
docker build -t medilabo/ms-front .\ms-front
```

#### 2) Créer un réseau Docker dédié

```powershell
docker network create medilabo-net
```

#### 3) Lancer MongoDB avec initialisation

```powershell
docker run -d --name mongo `
  --network medilabo-net `
  -p 27017:27017 `
  -e MONGO_INITDB_ROOT_USERNAME=root `
  -e MONGO_INITDB_ROOT_PASSWORD=example `
  -v ${PWD}\docker\mongo-init:/docker-entrypoint-initdb.d:ro `
  mongo:7
```

Notes :
- Par défaut, le script importe `docker/mongo-init/data.json` dans la base `notesdb`, collection `notes`.
- Vous pouvez surcharger via variables d'env : `MONGO_DB_NAME` et `MONGO_COLLECTION`.

#### 4) Lancer Eureka

```powershell
docker run -d --name ms-eureka `
  --network medilabo-net `
  -p 8761:8761 `
  medilabo/ms-eureka
```

Vérifiez le dashboard : http://localhost:8761

#### 5) Lancer les microservices Spring Boot

Adaptez les variables d'environnement de vos applications si nécessaire (URL BDD, Eureka, etc.). Exemple générique :

```powershell
# Gateway
docker run -d --name ms-gateway `
  --network medilabo-net `
  -p 8080:8080 `
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka `
  medilabo/ms-gateway

# Patient
docker run -d --name ms-patient `
  --network medilabo-net `
  -p 8081:8081 `
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka `
  medilabo/ms-patient

# Notes (MongoDB)
docker run -d --name ms-notes `
  --network medilabo-net `
  -p 8082:8082 `
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka `
  -e SPRING_DATA_MONGODB_URI=mongodb://root:example@mongo:27017/notesdb?authSource=admin `
  medilabo/ms-notes

# Risk
docker run -d --name ms-risk `
  --network medilabo-net `
  -p 8083:8083 `
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://ms-eureka:8761/eureka `
  medilabo/ms-risk
```

Remarques :
- Assurez-vous que les propriétés `application.yml/properties` correspondent (ex. SPRING_DATA_MONGODB_URI pour ms-notes, URL MySQL si ms-patient utilise MySQL, etc.).
- Les variables d'env Spring Boot peuvent être passées en mode `SPRING_...` (remplacez les points par des underscores).

#### 6) Lancer le frontend (Nginx)

```powershell
docker run -d --name ms-front `
  --network medilabo-net `
  -p 80:80 `
  medilabo/ms-front
```

Frontend : http://localhost

#### Vérification rapide

- Eureka : http://localhost:8761 — les services devraient s’enregistrer progressivement.
- Frontend : http://localhost — l’application web doit être accessible.
- API via Gateway : http://localhost:8080

#### Arrêt et nettoyage

```powershell
docker stop ms-front ms-risk ms-notes ms-patient ms-gateway ms-eureka mongo

docker rm ms-front ms-risk ms-notes ms-patient ms-gateway ms-eureka mongo

docker network rm medilabo-net
```


## Installation manuelle (sans Docker)

Cette approche est utile en développement local.

### 1) Démarrer MongoDB localement et importer les données

- Démarrez votre serveur MongoDB (local ou via un conteneur). Exemple via conteneur :

```powershell
docker run -d --name mongo-dev -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=example mongo:7
```

- Importez les données `data.json` :

```powershell
mongoimport `
  --username root `
  --password example `
  --authenticationDatabase admin `
  --db notesdb `
  --collection notes `
  --file .\docker\mongo-init\data.json `
  --jsonArray `
  --drop
```

Ajustez la chaîne de connexion Mongo dans `ms-notes` si nécessaire, par exemple :
- SPRING_DATA_MONGODB_URI=mongodb://root:example@localhost:27017/notesdb?authSource=admin

### 2) Lancer les microservices Spring Boot

Depuis chaque dossier de service :

```powershell
# Exemple pour ms-eureka
cd .\ms-eureka
mvn spring-boot:run

# Dans d'autres terminaux :
cd ..\ms-gateway; mvn spring-boot:run
cd ..\ms-patient; mvn spring-boot:run
cd ..\ms-notes; mvn spring-boot:run
cd ..\ms-risk; mvn spring-boot:run
```

- Si Eureka est activé côté clients, configurez `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka`.
- Pour `ms-notes`, configurez la connexion Mongo (ex. `SPRING_DATA_MONGODB_URI` ci‑dessus) ou via `application.yml`.

### 3) Lancer le frontend en mode dev

```powershell
cd .\ms-front
npm ci
npm run dev
```

Accédez à l’URL affichée par Vite (par défaut http://localhost:5173).

Pour une build de prod locale :

```powershell
npm run build
npx serve .\dist
```


## Dépannage

- Les services ne s’affichent pas dans Eureka : vérifiez la variable `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` et la connectivité réseau.
- `ms-notes` ne démarre pas : assurez-vous que MongoDB est accessible et que `SPRING_DATA_MONGODB_URI` est correctement défini.
- Conflits de ports : changez les ports mappés `-p` côté Docker ou les ports des services.
- Logs d’un service Docker :

```powershell
docker logs -f ms-gateway
```


## Structure utile du dépôt

- `ms-*/Dockerfile` : images des microservices
- `ms-front/nginx.conf` : config Nginx du frontend
- `docker/mongo-init/` : seed MongoDB (`data.json`) et script `init-mongo.sh`


## Licence

Ce projet est destiné à des fins éducatives/démo. Adapter selon vos besoins métier.
