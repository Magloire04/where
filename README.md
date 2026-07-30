# Après mon bac — Estimateur de bourse & d'orientation (Bénin)

Application web qui aide les **nouveaux bacheliers béninois** à estimer leurs chances
d'obtenir une **bourse** ou une **aide/FPP** dans les filières publiques, à partir de leur
série, de leurs notes et des quotas officiels du guide d'orientation du **MESRS**.

L'élève choisit sa série : les **matières de la série s'affichent automatiquement**, il saisit
ses notes (et coefficients), et l'outil classe les filières accessibles avec un **pourcentage
de chance estimé** et un court argumentaire. Aucune donnée personnelle n'est conservée.

> ⚠️ Les pourcentages sont des **estimations** (heuristique quotas + sélectivité + mentions),
> pas une garantie d'admission. Voir les limites dans [`data/README.md`](data/README.md).

## Architecture

| Composant | Stack | Dossier |
|-----------|-------|---------|
| Backend (API REST `/api/v1`) | Java 21 · Spring Boot 3.3 · Maven | [`backend/`](backend/) |
| Frontend (mobile-first) | React 19 · TypeScript · Vite | [`frontend/`](frontend/) |
| Données (guide MESRS) | JSON extrait du PDF officiel | [`data/`](data/) |

Le moteur charge au démarrage les **224 filières publiques** du guide **2026-2027**
(UAC, Parakou, UNSTIM, UNA et autres établissements publics), résout les 3 matières de
calcul par série, pondère la moyenne, puis estime les probabilités de bourse et d'aide.

## Données

Dataset extrait du guide officiel « Guido » 2026-2027 du MESRS. Détails, schéma d'une filière,
statistiques et limites : [`data/README.md`](data/README.md).

## Démarrer en local

### Prérequis

- JDK 21, Maven 3.9+
- Node.js 20+ et npm

### Backend

```bash
cd backend
mvn spring-boot:run
# API sur http://localhost:8080 — doc OpenAPI/Redoc à la racine
```

Détails (build, tests, OCR optionnel) : [`backend/README.md`](backend/README.md).

### Frontend

```bash
cd frontend
npm install
npm run dev
# Interface sur http://localhost:5173
```

Configurer l'URL de l'API si besoin via `frontend/.env.local` :

```
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Qualité & CI

- **Backend** : `mvn spotless:check test`
- **Frontend** : `npm run lint && npm test && npm run build`

La CI GitHub Actions exécute ces deux pipelines (format, lint, tests, build) sur chaque PR.

## Workflow de contribution

Gitflow : branches `feature/…` depuis `develop`, Conventional Commits, PR vers `develop`,
CI verte avant merge. `main` reçoit les versions promues depuis `develop`.

## Confidentialité (APDP)

Les notes saisies sont traitées le temps du calcul puis oubliées : aucune donnée personnelle
n'est stockée côté serveur.
