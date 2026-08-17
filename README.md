# Après mon bac : estimateur de bourse & d'orientation (Bénin)

Application web qui aide les **nouveaux bacheliers béninois** à estimer leurs chances
d'obtenir une **bourse** ou une **aide/FPP** dans les filières publiques, à partir de leur
série, de leurs meilleures notes et des quotas officiels du guide d'orientation du **MESRS**.

Le parcours : l'élève choisit sa **série de l'enseignement général** (A1, A2, B, C, D), déclare
ses **3 matières fortes** (avec note /20 et coefficient), et l'outil liste les **filières qui
calculent leur classement sur ces matières**, chacune avec un **statut estimé** (boursier ou aidé)
et un **pourcentage de chance**. Si une filière a besoin d'une matière non déclarée, la section
« Affine ton estimation » la réclame afin de calculer sur le **triplet complet exact**. Les
résultats sont regroupés (top 3 global, puis par catégorie d'établissement). Aucune donnée
personnelle n'est conservée.

> Les pourcentages sont des **estimations indicatives** (heuristique quotas + sélectivité), pas une
> garantie de classement par l'État. Limites détaillées dans [`data/README.md`](data/README.md).

## Architecture

| Composant | Stack | Dossier |
|-----------|-------|---------|
| Backend (API REST `/api/v1`) | Java 21 · Spring Boot 3.3 · Maven | [`backend/`](backend/) |
| Frontend (mobile-first) | React 19 · TypeScript · Vite | [`frontend/`](frontend/) |
| Données (guide MESRS) | JSON extrait du PDF officiel | [`data/`](data/) |

Le moteur charge au démarrage les **224 filières publiques** du guide **2026-2027** (UAC, Parakou,
UNSTIM, UNA et autres établissements publics), résout par série le triplet de matières de calcul de
chaque filière, ne retient que les filières partageant au moins 2 des 3 matières fortes déclarées,
calcule la moyenne pondérée sur le triplet complet, puis estime la chance d'allocation (bourse ou
aide) et classe les résultats.

## Données

Dataset extrait du guide officiel « Guido » 2026-2027 du MESRS, plus les coefficients officiels du
bac par série. Détails, schéma d'une filière et limites : [`data/README.md`](data/README.md).

## Démarrer en local

### Prérequis

- JDK 21, Maven 3.9+
- Node.js 20+ et npm

### Backend

```bash
cd backend
mvn spring-boot:run
# API sur http://localhost:8080 (doc OpenAPI/Redoc sur /docs.html)
```

Le port est configurable via la variable `PORT` (ou `SERVER_PORT`). Par exemple, si le port 8080 est
déjà pris (Docker Desktop, WAMP…), lance le backend sur 8081 :

```bash
# PowerShell : $env:SERVER_PORT='8081'   |   cmd : set SERVER_PORT=8081   |   bash : export SERVER_PORT=8081
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# Interface sur http://localhost:5173
```

Par défaut le frontend appelle `http://localhost:8080/api/v1`. Pour pointer un autre port, crée
`frontend/.env.local` :

```
VITE_API_BASE_URL=http://localhost:8081/api/v1
```

## Déploiement

L'application se déploie en **conteneur unique** : le backend Spring Boot sert aussi le frontend
statique (même origine, donc pas de CORS). Le `Dockerfile` à la racine construit le frontend, l'embarque
dans les ressources statiques du backend, puis produit l'image de run.

```bash
# Build et test local de l'image complète
docker build -t apresbac .
docker run --rm -p 8090:8080 apresbac
# http://localhost:8090 (SPA)  |  http://localhost:8090/api/v1/health -> {"status":"UP"}
```

En production, l'image est construite et exécutée sur **Spaceship Starlight Hyperlift** (build du
`Dockerfile` depuis la branche `main`). L'hébergeur fournit la variable `PORT` (déjà gérée) et le
TLS pour le domaine.

## Qualité & CI

- **Backend** : `mvn spotless:check test`
- **Frontend** : `npm run lint && npm test && npm run build`

La CI GitHub Actions exécute ces deux pipelines (format, lint, tests, build) sur chaque PR.

## Workflow de contribution

Gitflow : branches `feature/…` depuis `develop`, Conventional Commits, PR vers `develop`, CI verte
avant merge. `main` reçoit les versions promues depuis `develop` (branche déployée).

## Confidentialité

Les notes saisies sont traitées le temps du calcul puis oubliées : aucune donnée personnelle n'est
stockée côté serveur (ni base de données, ni journalisation des notes). Les pages Politique de
confidentialité, Conditions d'utilisation et Mentions légales sont accessibles depuis le pied de
page du site.
