# Dataset — Filières & allocations (Guide d'orientation 2026-2027, MESRS Bénin)

Données extraites du PDF officiel `GuideOrientation2026-2027.pdf` — « Guido », Guide d'information universitaire MESRS **2026-2027**, 92 pages (extraction texte `pdftotext -layout`, quotas et matières relus ligne à ligne). C'est la **nouvelle édition** fournie par le MESRS ; elle remplace l'édition 2025-2026 (`QuideOrientation2025-2026.pdf` / `Guide d'orientation.pdf`, conservés à la racine à titre d'archive).

## Contenu

| Fichier | Contenu | Volume |
|---|---|---|
| `raw/uac.json` | Université d'Abomey-Calavi (UAC) | 121 filières |
| `raw/parakou.json` | Université de Parakou (UP) | 34 filières |
| `raw/unstim.json` | Univ. Nat. des Sciences, Technologies, Ingénierie et Maths (UNSTIM) | 39 filières |
| `raw/una.json` | Université Nationale d'Agriculture (UNA) | 15 filières |
| `raw/autres_publics.json` | UADC, IUEP, Sèmè City, Écoles Inter-États | 15 entrées |
| `raw/epes_agrees.json` | EPES privés — filières agréées (référence, sans quota) | 39 établissements, 199 offres |
| `coefficients.json` | Grille de coefficients par série (⚠️ incomplète — voir plus bas) | — |

**Total filières publiques (avec quotas) : 224.**

## Statistiques (validation)

- **4 912 bourses** au total
- **7 292 places d'aide/FPP**
- **60 filières à 0 place d'aide/FPP** → sélection binaire « boursier OU entièrement payant »
- **164 filières avec coussin d'aide** (ex. FASEG Sciences Éco 10 bourses / 700 aides ; FADESP Droit 0 / 500)
- Modes d'entrée : 180 Classement · 42 Concours · 2 Dossier (Sèmè City)

## Nouveautés de l'édition 2026-2027 (vs 2025-2026)

- **224 offres** (contre 216) et réorganisation des sections : l'UADC devient une université à part
  entière (section V), l'UAC est renumérotée de 1 à 27 établissements.
- **Quotas révisés** : ex. Médecine Générale **95/20** (au lieu de 150/0), Droit FADESP **0/500**.
- **Format « matières par série » explicite** : le guide donne désormais, pour chaque filière, les
  matières écrites **groupées par série** (ex. `C, D : Maths, PCT, SVT`). `matieres_raw` reproduit ce
  format ; `MatiereResolver` sélectionne la clause de la série de l'élève.

## Schéma d'une filière (fichiers universités)

```json
{
  "num": 13,
  "etablissement": "Faculté des Sciences de la Santé (FSS)",
  "filiere": "Médecine Générale",
  "quota_bourse": 95,           // nb de bourses (int)
  "quota_aide_fpp": 20,         // nb de places aide/FPP (int) — 0 = pas de coussin
  "mode_entree": "Classement",  // "Classement" | "Concours" | "Dossier"
  "series_bac_raw": "C, D",     // séries acceptées (verbatim)
  "matieres_raw": "C, D : Maths, PCT, SVT", // matières par série (verbatim, clauses séparées par « | »)
  "debouches": ["Médecin généraliste", "..."],
  "page": 9                     // page du guide (traçabilité)
}
```

`matieres_raw` conserve le texte du guide, groupé par série et séparé par « | »
(ex. `"DEAT/Foresterie, DEAT/PV : Les trois (03) matières écrites | C, D : Maths, PCT, SVT"`).
`MatiereResolver.resoudre(matieres_raw, serie)` choisit la clause dont le préfixe contient la série,
puis canonicalise chaque libellé via `SubjectDictionary`.

## ⚠️ Limites & pièces manquantes (à traiter avant la mise en production)

1. **Grille de coefficients incomplète** (`coefficients.json`). Le guide ne publie que les triplets
   scientifiques des séries **C** et **D** (Maths/PCT/SVT). La grille officielle complète
   (matière × série) doit être obtenue auprès de l'**Office du Baccalauréat**. C'est le blocage n°1
   pour un calcul exact ; les coefficients ne sont pré-remplis que pour C et D.
2. **Aucune moyenne de coupure historique** par filière (le guide n'en contient pas). Le
   « % de chance » est un **estimateur** (heuristique quotas + sélectivité + mentions), pas une garantie.
3. **EPES agréés sans quotas** : le guide ne donne ni quota, ni série, ni matière pour le privé
   agréé — seulement établissement + offres. Non exploitable pour le moteur de bourse (référence uniquement).
4. **Séries techniques/agricoles** (F, DT, DEAT, EA) : `matieres_raw` indique souvent « Les trois (03)
   matières écrites » (non résoluble automatiquement) → non calculées ; le champ « + Ajouter une
   matière » de l'interface sert de filet.
