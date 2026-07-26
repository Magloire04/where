# Spec — Application d'orientation & estimation de bourse (bacheliers du Bénin)

- **Date** : 2026-07-26
- **Statut** : Design validé (brainstorming) — prêt pour le plan d'implémentation
- **Édition des données** : Guide d'orientation MESRS **2025-2026**

## 1. Contexte & objectif

Aider un nouveau bachelier béninois à choisir la filière qui **maximise sa chance d'obtenir une allocation** (bourse ou aide/FPP). Le bachelier fournit sa **série** et ses **notes** (upload de relevé ou saisie manuelle) ; l'application calcule sa **moyenne de classement** pour chaque filière compatible, **estime son statut probable** (boursier / aide-demi-boursier / payant) et retourne **3 propositions** avec un **pourcentage estimé** et un **argumentaire** de quelques lignes.

Données source : dataset extrait du guide officiel — **216 filières publiques** (avec quotas bourse & aide/FPP), voir `data/README.md`. Totaux validés vs officiel : **6 989 bourses**, **11 853 places aide/FPP**.

## 2. Décisions validées

| Sujet | Décision |
|---|---|
| Backend | **Spring Boot / Java** |
| Frontend | **React (SPA)** |
| Base de données | **MySQL** — statistiques anonymes agrégées **uniquement** |
| OCR | **Tesseract via Tess4J** (local, gratuit, aucun coût par appel) |
| Argumentaire | **Templates rule-based** (aucun LLM à l'exécution) |
| Coefficients | **Lus/saisis depuis le relevé du candidat** (note + coefficient par matière) — contourne la grille officielle manquante |
| Reco | **Hybride** : top-3 tous domaines par défaut + filtre domaine optionnel + statut affiché |
| Données perso | **Aucun compte, aucun relevé conservé** ; seulement stats anonymes |
| Estimateur % | **Seuils estimés par palier de sélectivité** + fonction logistique |

**Contrainte transverse** : à l'exécution, **zéro appel Claude/LLM** (OCR local + argumentaire template). Tout tourne sur le serveur sans coût par requête.

## 3. Parcours utilisateur (V1)

1. **Choix de la série** (A1, A2, B, C, D, E, F1-F4, G1-G3, DT/DEAT…) — obligatoire.
2. **Saisie des notes**, au choix :
   - **Upload** image/PDF du relevé → OCR → tableau *(matière, note, coefficient)* **éditable** ;
   - **Saisie manuelle** ligne par ligne.
3. **Filtre domaine optionnel** (santé, droit, gestion, sciences, agro, lettres, technologie…).
4. **Calcul** → évaluation de toutes les filières compatibles avec la série.
5. **Résultat** : **top-3** (meilleures cotes) chacune avec **statut estimé + % + argumentaire** ; liste d'**alternatives** ; sections séparées pour filières **à concours** et **à titre payant**.
6. Incrément **stats anonymes**.

## 4. Architecture & modules

```
React (SPA)
  └─ REST/JSON ──> Spring Boot API
        ├─ OcrController            POST /api/ocr        (image/PDF → matières+notes+coef)   [Tess4J]
        ├─ RecommendationController POST /api/recommander (série+notes+filtre → top-3 + alternatives)
        ├─ MetaController           GET  /api/series, GET /api/domaines
        ├─ Modules métier :
        │   ├─ FiliereRepository    (charge le dataset JSON en mémoire au démarrage)
        │   ├─ MatiereResolver      (matieres_resolues[serie] → 3 matières canoniques)
        │   ├─ MoyenneCalculator    (notes+coef candidat → moyenne pondérée par filière)
        │   ├─ SelectiviteTiering   (filière → palier + seuils estimés)
        │   ├─ ProbabilityEstimator (moyenne + seuils → P(bourse)/P(aide)/P(payant))   [interface stable]
        │   ├─ Recommender          (filtre hybride, score, top-3 + alternatives)
        │   └─ ArgumentaireBuilder  (templates rule-based)
        └─ StatsService ──> MySQL   (compteurs anonymes agrégés)
```

- Dataset (`data/raw/*.json` + `coefficients.json` + mapping enrichi) embarqué en ressource, chargé en mémoire au démarrage (volume léger).
- Chaque module = responsabilité unique, testable isolément.
- `ProbabilityEstimator` expose une **interface stable** pour brancher le calibrage live apresmonbac en V2 sans toucher au reste.
- **Filières à concours (42)** : sélection hors classement-moyenne → marquées « entrée par concours, non estimable », exclues du classement probabiliste, listées à part.
- **Filières à titre payant (8 : UADC, Sèmè City)** : aucune bourse → exclues de l'optimisation, listées en info.

## 5. Données : résolution matières & coefficients

- **Pré-traitement** : enrichir chaque filière d'un champ `matieres_resolues`, ex.
  `{ "C": ["MATHS","PCT","SVT"], "D": ["MATHS","PCT","SVT"], "A1": ["FR","ANG","PHILO"] }`,
  dérivé de `matieres_raw` en interprétant : conditions (« Pour C et D… | Pour DEAT… »), substitutions (« Maths (LV1 pour A et Economie pour B) », « ou Etude de cas (G) »).
- **Dictionnaire de synonymes** → vocabulaire canonique :
  `MATHS, PCT (=SPCT), SVT, FR (Français), PHILO, HG (Hist-Géo), ANG (LV1), ANG2 (LV2), ESP, ALL, ECO (Economie), EDC (Etude de cas), CG (Culture générale)…`
  Utilisé pour rapprocher les libellés du **relevé** et ceux du **guide**.
- **MoyenneCalculator** : pour une filière, prendre `matieres_resolues[serie]` ; pour chaque matière canonique, récupérer *note + coefficient* saisis par le candidat ; `M = Σ(note·coef) / Σ(coef)`.
  Si une matière requise est absente des notes du candidat → filière marquée **« données insuffisantes »** (aucune valeur inventée, filière listée à part).

## 6. Estimateur par palier (calcul du %)

**SelectiviteTiering** classe chaque filière en 4 paliers via : `quota_bourse`, présence/taille du coussin d'aide (`quota_aide_fpp`), et un **drapeau prestige curé** (liste : médecine, pharmacie, kinésithérapie, prépas MPSI/PCSI/INSPEI, droit, sciences éco, informatique/IFRI, statistique, IRSP…). Seuils **paramétrables** (fichier config), sur /20 :

| Palier | Exemples | seuil_bourse | seuil_aide |
|---|---|---|---|
| T1 Très sélectif | Médecine, Pharmacie, Prépas, Kiné | 15.0 | — |
| T2 Sélectif | Droit, Sciences Éco, Informatique | 13.0 | 11.5 |
| T3 Moyen | facultés générales, quota moyen | 11.5 | 10.5 |
| T4 Ouvert | gros quota + gros coussin d'aide | 10.5 | 10.0 |

(Valeurs initiales indicatives, ajustables sans recompilation.)

**Règle d'affectation de palier** (heuristique, dans SelectiviteTiering) :
1. si `prestige` → T1 si (quota_bourse petit **ou** aide=0) sinon T2 ;
2. sinon si `quota_aide_fpp ≥ 3 × quota_bourse` (gros coussin) → T4 ;
3. sinon si `quota_aide_fpp > 0` → T3 ;
4. sinon (aide=0, non prestige) → T3 par défaut (petit quota générique).

**ProbabilityEstimator** (étalement σ = 1.2, paramétrable) :
- `P(bourse) = clamp(logistique((M − seuil_bourse)/σ), 0.02, 0.98)`
- si `quota_aide_fpp > 0` : `P(aide) = (1 − P(bourse)) · logistique((M − seuil_aide)/σ)` ; sinon `P(aide) = 0`
- `P(payant) = 1 − P(bourse) − P(aide)`
- **statut affiché** = catégorie de probabilité maximale ; **% affiché** = sa probabilité.

où `logistique(x) = 1 / (1 + e^(−x))`.

**Repli seuil_aide** : si le palier ne définit pas de `seuil_aide` (cas T1) alors que `quota_aide_fpp > 0`, utiliser `seuil_aide = seuil_bourse − 1.0`. (En pratique les filières T1 ont un coussin d'aide nul, donc `P(aide)=0` ; ce repli ne couvre qu'un cas résiduel.)

**Recommender (hybride)** :
- éligibilité = série ∈ séries acceptées (+ filtre domaine optionnel) ;
- exclut concours & payant du classement probabiliste (retournés dans des listes séparées) ;
- `score = P(bourse) + 0.5 · P(aide)` ; tri décroissant ;
- retourne **top-3** + jusqu'à 5 **alternatives** ;
- chaque élément : filière, établissement, statut estimé, %, argumentaire, quotas, débouchés.

## 7. Argumentaire (templates rule-based)

`ArgumentaireBuilder` produit 2–4 phrases à partir des données (aucun LLM). Modèles selon le statut :
- **Boursier** : « Avec une moyenne estimée de {M}/20 en {m1, m2, m3}, tu dépasses le seuil de bourse estimé (~{Sb}) de {filière} à {établissement} ({B} bourses, filière {palier}). Débouchés : {débouchés courts}. »
- **Aide** : « Ta moyenne {M}/20 est proche du seuil de bourse (~{Sb}) mais au-dessus du seuil d'aide (~{Sa}) ; {filière} offre {A} places d'aide/FPP, d'où une bonne chance de sélection en aide. »
- **Payant** : « Ta moyenne {M}/20 reste sous les seuils estimés de {filière} ; sélection en boursier peu probable ici — vois les alternatives à meilleures cotes. »
- **Concours** : « {filière} recrute par **concours**, pas au classement : la moyenne n'est pas déterminante, prépare l'épreuve. »
- **Disclaimer systématique** : « Estimation indicative, pas une garantie de sélection. »

## 8. OCR

`POST /api/ocr` (multipart image/PDF) → Tess4J (traineddata `fra`) → texte → parsing regex en lignes *(matière, note, coefficient)* → renvoi d'un **tableau éditable**. Les relevés variant, l'utilisateur **corrige toujours** avant calcul. Parsing faible/échec → message + repli sur saisie manuelle. Fichier traité en mémoire/temp puis **supprimé immédiatement** après extraction.

## 9. Statistiques anonymes & APDP

- MySQL, **agrégats uniquement** : compteur de simulations, répartition par série, filières les plus recommandées, usage du filtre domaine.
- **Aucune** donnée personnelle stockée (ni nom, ni notes, ni date de naissance, ni relevé).
- Fichiers uploadés supprimés immédiatement après OCR.
- Bandeau de confidentialité clair affiché au premier usage.
- Conforme à l'esprit de la loi APDP n°2017-20 (pas de traitement de données personnelles conservées).

## 10. Erreurs & cas limites

- Note hors [0, 20] → erreur de validation côté API et front.
- Matière requise manquante pour une filière → « données insuffisantes », filière listée à part (jamais estimée à tort).
- OCR en échec → repli explicite sur saisie manuelle.
- Filière à concours → signalée non estimable.
- Filière à titre payant → signalée sans bourse.
- Moyenne très basse → retourne honnêtement les meilleures cotes disponibles (souvent aide/payant), sans fausse promesse.

## 11. Stratégie de tests

- **Unitaires** :
  - `MatiereResolver` : chaque pattern de `matieres_raw` (simple, conditionnel, substitution).
  - `MoyenneCalculator` : **golden tests** = Médecine Bac D (SVT×5, Maths×4, SPCT×4 → ÷13) et Bac C (SVT×2, Maths×6, SPCT×5 → ÷13) tirés du guide.
  - `SelectiviteTiering` : affectation des paliers sur cas types.
  - `ProbabilityEstimator` : monotonie (M↑ ⇒ P(bourse)↑), bornes [2 %, 98 %], `P(aide)=0` si `quota_aide_fpp=0`, somme des 3 probas = 1.
  - `Recommender` : tri par score, exclusion concours/payant, filtre domaine.
  - `ArgumentaireBuilder` : présence du disclaimer, variantes par statut.
- **Intégration** : `/api/recommander` bout-en-bout sur profils types (ex. élève D fort → médecine boursier ; élève A faible → lettres en aide).
- **OCR** : un relevé exemple (extraction + repli manuel).

## 12. Hors périmètre V1 (V2+)

Comptes/historique ; calibrage live apresmonbac ; EPES « régime ouverture » (pages 82-98) ; dépendance à la grille officielle de coefficients ; multi-langue ; appli mobile native ; argumentaire par LLM ; version enrichie Claude optionnelle.

## 13. Points ouverts / paramètres à ajuster

- **Seuils par palier** et **σ** : valeurs initiales indicatives (§6), à calibrer avec des cas réels dès que possible.
- **Liste prestige** : à réviser (curée manuellement).
- **Grille de coefficients officielle** (Office du Bac) : non requise pour V1 (coefficients lus du relevé), mais utile comme repli/validation — voir `data/coefficients.json` (incomplet, valeurs C/D confirmées).
- **Parsing OCR** : les gabarits de relevés béninois varient ; le tableau éditable absorbe l'imperfection en V1.
