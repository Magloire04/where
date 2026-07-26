# Dataset — Filières & allocations (Guide d'orientation 2025-2026, MESRS Bénin)

Données extraites du PDF officiel `Guide d'orientation.pdf` (98 pages, rendu page→image puis lecture, quotas vérifiés visuellement).

## Contenu

| Fichier | Contenu | Volume |
|---|---|---|
| `raw/uac.json` | Université d'Abomey-Calavi (UAC) | 115 filières |
| `raw/parakou.json` | Université de Parakou (UP) | 33 filières |
| `raw/unstim.json` | Univ. Nat. des Sciences, Technologies, Ingénierie et Maths (UNSTIM) | 38 filières |
| `raw/una.json` | Université Nationale d'Agriculture (UNA) | 15 filières |
| `raw/autres_publics.json` | IUEP, Écoles Inter-États, UADC, Sèmè City | 15 entrées |
| `raw/epes_agrees.json` | EPES privés — filières agréées (référence, sans quota) | 39 établissements, 199 offres |
| `coefficients.json` | Grille de coefficients par série (⚠️ incomplète — voir plus bas) | — |

**Total filières publiques (avec quotas) : 216** — dont 8 « à titre payant » (UADC, Sèmè City) sans bourse.

## Statistiques (validation)

- **6 989 bourses** au total (chiffre officiel rapporté : ~6 900 ✅)
- **11 853 places d'aide/FPP** (chiffre officiel rapporté : ~11 800 ✅)
- **166 filières à 0 place d'aide/FPP** → sélection binaire « boursier OU entièrement payant »
- **38 filières avec coussin d'aide** (ex. FASEG Sciences Éco 207 bourses / 1407 aides ; FADESP Droit 104 / 999)
- Modes d'entrée : 166 Classement · 42 Concours · 8 à titre payant

## Schéma d'une filière (fichiers universités)

```json
{
  "num": 17,
  "etablissement": "Faculté des Sciences de la Santé (FSS)",
  "filiere": "Médecine Générale",
  "quota_bourse": 150,          // nb de bourses (int)
  "quota_aide_fpp": 0,          // nb de places aide/FPP (int) — 0 = pas de coussin
  "mode_entree": "Classement",  // "Classement" | "Concours" | "A titre payant"
  "series_bac_raw": "C, D",     // séries acceptées (verbatim)
  "matieres_raw": "Maths / PCT / SVT", // 3 matières de calcul (verbatim, avec conditions par série)
  "debouches": ["Médecin généraliste", "..."],
  "page": 31                    // page du guide (traçabilité)
}
```

`matieres_raw` conserve le texte du guide, y compris les substitutions conditionnelles par série
(ex. `"Maths (LV1 pour A et Economie pour B) / Français / Hist-Géo"`). Un parsing structuré
`matieres[serie] = [m1, m2, m3]` reste à dériver pour l'automatisation du calcul.

## ⚠️ Limites & pièces manquantes (à traiter avant la mise en production)

1. **Grille de coefficients incomplète** (`coefficients.json`). Le guide ne publie que 2 exemples
   (Médecine séries C et D). La grille officielle complète (matière × série) doit être obtenue
   auprès de l'**Office du Baccalauréat**. C'est le blocage n°1 pour un calcul exact.
2. **Aucune moyenne de coupure historique** par filière (le guide n'en contient pas). Le
   « % de chance » devra être un **estimateur** (heuristique quotas + sélectivité + mentions),
   ou s'appuyer sur les compteurs live d'apresmonbac pendant la fenêtre de choix. Ce n'est PAS une garantie.
3. **EPES agréés sans quotas** : le guide ne donne ni quota, ni série, ni matière pour le privé
   agréé — seulement établissement + offres. Non exploitable pour le moteur de bourse (référence uniquement).
4. **EPES « régime ouverture » (pages 82-98) non extraits** (hors périmètre, entièrement payants — phase 2).
5. Édition **2025-2026** (l'édition 2026-2027 n'était pas publiée à la date de l'extraction).
