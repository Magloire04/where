# orientation-backend

Moteur d'estimation de bourse pour bacheliers (Spring Boot / Java 21).
Voir la spec : `../docs/superpowers/specs/2026-07-26-orientation-bacheliers-benin-design.md`.

## Prérequis
- JDK 21
- Maven 3.9+

## Build & tests

```bash
mvn test
```

### Note environnement local (interception SSL)

Sur certaines machines (proxy/antivirus qui inspecte le HTTPS), Maven échoue au
téléchargement des dépendances avec `PKIX path building failed` : la JVM n'a pas
le CA d'interception dans son truststore. Solution — faire utiliser à Maven le
magasin de certificats **Windows** :

```powershell
$env:MAVEN_OPTS = '-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT'
mvn test
```

Ce réglage est **local à ce poste** et n'est pas committé : la CI (Linux) n'en a
pas besoin.

## API
Base : `/api/v1`. Enveloppe de réponse standard ASIN `{ "data": … }` / erreurs
`{ "error": { "code", "message", "status" } }`. Contrat : `openapi.yaml`.
