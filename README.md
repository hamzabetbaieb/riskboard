# RiskBoard

Application fullstack pour le suivi des limites de risque et la gestion des demandes de dérogation.

Monorepo :
- `riskboard` : backend Java 21 / Spring Boot 4 / Maven
- `riskboard-front` : frontend Angular 22

## Prérequis

- Java 21+
- Node.js + npm
- Docker Desktop (pour PostgreSQL)

## Base de données (PostgreSQL)

Depuis la racine du dépôt :

```powershell
docker compose up -d postgres
```

Paramètres par défaut :
- hôte : `localhost`
- port : `5432`
- base : `riskboard`
- utilisateur : `riskboard`
- mot de passe : `riskboard`

Le backend utilise ces variables d'environnement (optionnelles) :
- `DB_URL` (par défaut `jdbc:postgresql://localhost:5432/riskboard`)
- `DB_USERNAME` (par défaut `riskboard`)
- `DB_PASSWORD` (par défaut `riskboard`)

## Lancer le backend

```powershell
Set-Location .\riskboard
.\mvnw.cmd spring-boot:run
```

API backend : `http://localhost:8080`

## Lancer le frontend

```powershell
Set-Location .\riskboard-front
npm.cmd install
npm.cmd start
```

UI frontend : `http://localhost:4200`

## Vérification rapide du flux CSV

1. Ouvrir l'écran **Import CSV** dans le frontend.
2. Sélectionner un fichier CSV conforme au format attendu.
3. Cliquer sur **Importer**.
4. Vérifier :
   - le message de résumé (`lignes en succès`, `lignes en erreur`, `première erreur` si présente),
   - la mise à jour du dashboard des risques,
   - la persistance en base (tables `counterparties` et `risk_limits`).

## Build et tests

### Backend

```powershell
Set-Location .\riskboard
.\mvnw.cmd test
```

### Frontend

```powershell
Set-Location .\riskboard-front
npm.cmd run build
npm.cmd test -- --watch=false
```

## Pipeline CI

Le fichier `.gitlab-ci.yml` à la racine couvre :
- build backend (`mvn compile`)
- build frontend (`npm ci && npm run build`)
- tests backend (avec service PostgreSQL)
- tests frontend
