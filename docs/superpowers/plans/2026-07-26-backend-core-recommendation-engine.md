# Backend Core — Moteur de recommandation & API — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construire un service Spring Boot qui, à partir d'une série de bac et d'une liste de notes+coefficients, calcule la moyenne de classement par filière, estime le statut (boursier/aide/payant) et renvoie les 3 meilleures recommandations avec argumentaire — le tout sans aucun appel externe.

**Architecture:** Une API REST Spring Boot charge en mémoire au démarrage le dataset JSON des 216 filières publiques. Une chaîne de modules à responsabilité unique (résolution des matières → moyenne pondérée → palier de sélectivité → probabilités logistiques → classement hybride → argumentaire) transforme la requête en réponse. Aucune base de données ni OCR dans ce plan (sous-systèmes séparés).

**Tech Stack:** Java 21, Spring Boot 3.3.x, Maven, Jackson (JSON), JUnit 5 + AssertJ, Spring MVC Test (MockMvc).

## Global Constraints

- **Java 21**, Spring Boot **3.3.x**, build **Maven**.
- **Zéro appel Claude/LLM/réseau à l'exécution** : toute la logique est locale et déterministe.
- Dataset source : `data/raw/{uac,parakou,unstim,una,autres_publics}.json` (216 filières), copié dans `backend/src/main/resources/data/`.
- Paramètres de l'estimateur (**seuils par palier, σ, liste prestige**) **externalisés** dans `application.yml` — modifiables sans recompilation.
- **Aucune donnée personnelle stockée** (ce plan ne persiste rien).
- Vocabulaire métier en **français** ; le disclaimer « Estimation indicative, pas une garantie de sélection. » apparaît dans chaque argumentaire.
- Filières `mode_entree = "Concours"` → non estimables (listées à part). `mode_entree = "A titre payant"` → sans bourse (listées à part).

---

## File Structure

```
backend/
  pom.xml
  src/main/java/bj/orientation/
    OrientationApplication.java          # main Spring Boot
    model/
      Serie.java                         # enum des séries
      NoteSaisie.java                    # record (libellé, note, coef) — entrée
      MatiereNote.java                   # record (canonique, note, coef) — normalisé
      Filiere.java                       # record filière (dataset)
      ModeEntree.java                    # enum CLASSEMENT/CONCOURS/PAYANT
      StatutEstime.java                  # enum BOURSIER/AIDE/PAYANT/CONCOURS/PAYANT_UNIQUEMENT
      Palier.java                        # enum T1..T4
      Probabilites.java                  # record pBourse/pAide/pPayant/statut/pctAffiche
      Recommandation.java                # record filière+moyenne+proba+argumentaire
      RecommandationRequest.java         # record serie+notes+domaine
      RecommandationResponse.java        # record top3/alternatives/concours/payantes/insuffisantes
    data/
      FiliereRepository.java             # charge et expose le dataset
      SubjectDictionary.java             # synonymes → canonique
      SerieMatcher.java                  # série candidat ∈ series_bac_raw ?
      DomaineClassifier.java             # filière → domaine
    calc/
      MatiereResolver.java               # matieres_raw + série → 3 matières canoniques
      MoyenneCalculator.java             # notes+coef → moyenne pondérée
      SelectiviteTiering.java            # filière → palier
      ProbabilityEstimator.java          # moyenne + seuils → probabilités
      ArgumentaireBuilder.java           # templates rule-based
      Recommender.java                   # orchestration + classement hybride
    config/
      EstimateurProperties.java          # @ConfigurationProperties (seuils, σ, prestige)
    web/
      MetaController.java                # GET /api/series, /api/domaines
      RecommandationController.java      # POST /api/recommander
      GlobalExceptionHandler.java        # 400 sur validation
  src/main/resources/
    application.yml                      # paramètres estimateur
    data/*.json                          # dataset copié
  src/test/java/bj/orientation/...       # tests miroir
```

Chaque fichier a une responsabilité unique. `ProbabilityEstimator` et `SelectiviteTiering` sont isolés pour permettre le calibrage live (V2) sans toucher au reste.

---

### Task 1: Squelette Spring Boot runnable

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/bj/orientation/OrientationApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Test: `backend/src/test/java/bj/orientation/OrientationApplicationTest.java`

**Interfaces:**
- Produces: application Spring Boot démarrable ; contexte chargeable.

- [ ] **Step 1: Écrire le test de démarrage (échoue)**

```java
// backend/src/test/java/bj/orientation/OrientationApplicationTest.java
package bj.orientation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrientationApplicationTest {
    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 2: Créer `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>
    <groupId>bj.orientation</groupId>
    <artifactId>orientation-backend</artifactId>
    <version>0.1.0</version>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: Créer la classe principale et `application.yml`**

```java
// backend/src/main/java/bj/orientation/OrientationApplication.java
package bj.orientation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrientationApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrientationApplication.class, args);
    }
}
```

```yaml
# backend/src/main/resources/application.yml
server:
  port: 8080
spring:
  application:
    name: orientation-backend
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=OrientationApplicationTest`
Expected: BUILD SUCCESS, contextLoads passe.

- [ ] **Step 5: Commit**

```bash
git add backend/pom.xml backend/src
git commit -m "feat(backend): squelette Spring Boot runnable"
```

---

### Task 2: Enums et records du modèle

**Files:**
- Create: `backend/src/main/java/bj/orientation/model/Serie.java`
- Create: `backend/src/main/java/bj/orientation/model/ModeEntree.java`
- Create: `backend/src/main/java/bj/orientation/model/StatutEstime.java`
- Create: `backend/src/main/java/bj/orientation/model/Palier.java`
- Create: `backend/src/main/java/bj/orientation/model/NoteSaisie.java`
- Create: `backend/src/main/java/bj/orientation/model/MatiereNote.java`
- Create: `backend/src/main/java/bj/orientation/model/Filiere.java`
- Test: `backend/src/test/java/bj/orientation/model/ModeEntreeTest.java`

**Interfaces:**
- Produces:
  - `enum Serie { A1,A2,B,C,D,E,F1,F2,F3,F4,G1,G2,G3,DT,DEAT,EA }` avec `static Serie fromCode(String)`.
  - `enum ModeEntree { CLASSEMENT, CONCOURS, PAYANT }` avec `static ModeEntree parse(String)`.
  - `enum StatutEstime { BOURSIER, AIDE, PAYANT, CONCOURS, PAYANT_UNIQUEMENT }`.
  - `enum Palier { T1, T2, T3, T4 }`.
  - `record NoteSaisie(String libelle, double note, double coefficient)`.
  - `record MatiereNote(String canonique, double note, double coefficient)`.
  - `record Filiere(int num, String universite, String etablissement, String filiere, int quotaBourse, int quotaAideFpp, ModeEntree modeEntree, String seriesBacRaw, String matieresRaw, java.util.List<String> debouches, int page)`.

- [ ] **Step 1: Écrire le test de `ModeEntree.parse` (échoue)**

```java
// backend/src/test/java/bj/orientation/model/ModeEntreeTest.java
package bj.orientation.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ModeEntreeTest {
    @Test
    void parseMappeLesLibellesDuGuide() {
        assertThat(ModeEntree.parse("Classement")).isEqualTo(ModeEntree.CLASSEMENT);
        assertThat(ModeEntree.parse("Concours")).isEqualTo(ModeEntree.CONCOURS);
        assertThat(ModeEntree.parse("A titre payant")).isEqualTo(ModeEntree.PAYANT);
    }
}
```

- [ ] **Step 2: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=ModeEntreeTest`
Expected: FAIL — `ModeEntree` n'existe pas.

- [ ] **Step 3: Créer les enums et records**

```java
// Serie.java
package bj.orientation.model;
public enum Serie {
    A1, A2, B, C, D, E, F1, F2, F3, F4, G1, G2, G3, DT, DEAT, EA;
    public static Serie fromCode(String code) {
        if (code == null) throw new IllegalArgumentException("série nulle");
        return Serie.valueOf(code.trim().toUpperCase());
    }
}
```

```java
// ModeEntree.java
package bj.orientation.model;
public enum ModeEntree {
    CLASSEMENT, CONCOURS, PAYANT;
    public static ModeEntree parse(String raw) {
        if (raw == null) return CLASSEMENT;
        String s = raw.trim().toLowerCase();
        if (s.startsWith("concours")) return CONCOURS;
        if (s.startsWith("a titre payant") || s.startsWith("à titre payant")) return PAYANT;
        return CLASSEMENT;
    }
}
```

```java
// StatutEstime.java
package bj.orientation.model;
public enum StatutEstime { BOURSIER, AIDE, PAYANT, CONCOURS, PAYANT_UNIQUEMENT }
```

```java
// Palier.java
package bj.orientation.model;
public enum Palier { T1, T2, T3, T4 }
```

```java
// NoteSaisie.java
package bj.orientation.model;
public record NoteSaisie(String libelle, double note, double coefficient) {}
```

```java
// MatiereNote.java
package bj.orientation.model;
public record MatiereNote(String canonique, double note, double coefficient) {}
```

```java
// Filiere.java
package bj.orientation.model;
import java.util.List;
public record Filiere(
        int num, String universite, String etablissement, String filiere,
        int quotaBourse, int quotaAideFpp, ModeEntree modeEntree,
        String seriesBacRaw, String matieresRaw, List<String> debouches, int page) {}
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=ModeEntreeTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/bj/orientation/model backend/src/test/java/bj/orientation/model
git commit -m "feat(model): enums et records du domaine"
```

---

### Task 3: Chargement du dataset (FiliereRepository)

**Files:**
- Create: `backend/src/main/resources/data/uac.json` … (copie des 5 fichiers publics depuis `data/raw/`)
- Create: `backend/src/main/java/bj/orientation/data/FiliereRepository.java`
- Test: `backend/src/test/java/bj/orientation/data/FiliereRepositoryTest.java`

**Interfaces:**
- Consumes: `Filiere`, `ModeEntree`.
- Produces: `FiliereRepository` (bean Spring) avec `List<Filiere> toutes()`.

- [ ] **Step 1: Copier le dataset dans les ressources**

```bash
mkdir -p backend/src/main/resources/data
cp data/raw/uac.json data/raw/parakou.json data/raw/unstim.json data/raw/una.json data/raw/autres_publics.json backend/src/main/resources/data/
```

- [ ] **Step 2: Écrire le test (échoue)**

```java
// backend/src/test/java/bj/orientation/data/FiliereRepositoryTest.java
package bj.orientation.data;

import bj.orientation.model.ModeEntree;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class FiliereRepositoryTest {
    private final FiliereRepository repo = new FiliereRepository();

    @Test
    void chargeLes216FilieresPubliques() {
        assertThat(repo.toutes()).hasSize(216);
    }

    @Test
    void medecineGeneraleEstPresenteAvec150Bourses() {
        var med = repo.toutes().stream()
            .filter(f -> f.filiere().equals("Médecine Générale"))
            .findFirst().orElseThrow();
        assertThat(med.quotaBourse()).isEqualTo(150);
        assertThat(med.modeEntree()).isEqualTo(ModeEntree.CLASSEMENT);
    }
}
```

- [ ] **Step 3: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=FiliereRepositoryTest`
Expected: FAIL — `FiliereRepository` n'existe pas.

- [ ] **Step 4: Implémenter `FiliereRepository`**

```java
// backend/src/main/java/bj/orientation/data/FiliereRepository.java
package bj.orientation.data;

import bj.orientation.model.Filiere;
import bj.orientation.model.ModeEntree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FiliereRepository {
    private static final String[] FICHIERS = {
        "uac.json", "parakou.json", "unstim.json", "una.json", "autres_publics.json"
    };
    private final List<Filiere> filieres;

    public FiliereRepository() {
        this.filieres = charger();
    }

    public List<Filiere> toutes() {
        return filieres;
    }

    private List<Filiere> charger() {
        ObjectMapper mapper = new ObjectMapper();
        List<Filiere> res = new ArrayList<>();
        for (String fichier : FICHIERS) {
            try (InputStream in = getClass().getResourceAsStream("/data/" + fichier)) {
                if (in == null) throw new IllegalStateException("Ressource absente: /data/" + fichier);
                JsonNode root = mapper.readTree(in);
                String universite = root.path("universite").asText(root.path("groupe").asText(""));
                for (JsonNode n : root.path("filieres")) {
                    List<String> debouches = new ArrayList<>();
                    n.path("debouches").forEach(d -> debouches.add(d.asText()));
                    res.add(new Filiere(
                        n.path("num").asInt(),
                        universite,
                        n.path("etablissement").asText(""),
                        n.path("filiere").asText(""),
                        n.path("quota_bourse").asInt(0),
                        n.path("quota_aide_fpp").asInt(0),
                        ModeEntree.parse(n.path("mode_entree").asText("Classement")),
                        n.path("series_bac_raw").asText(""),
                        n.path("matieres_raw").asText(""),
                        debouches,
                        n.path("page").asInt(0)
                    ));
                }
            } catch (Exception e) {
                throw new IllegalStateException("Échec chargement " + fichier, e);
            }
        }
        return res;
    }
}
```

- [ ] **Step 5: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=FiliereRepositoryTest`
Expected: PASS (216 filières, Médecine 150 bourses).

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/resources/data backend/src/main/java/bj/orientation/data/FiliereRepository.java backend/src/test/java/bj/orientation/data/FiliereRepositoryTest.java
git commit -m "feat(data): chargement du dataset des 216 filières"
```

---

### Task 4: Dictionnaire de synonymes (SubjectDictionary)

**Files:**
- Create: `backend/src/main/java/bj/orientation/data/SubjectDictionary.java`
- Test: `backend/src/test/java/bj/orientation/data/SubjectDictionaryTest.java`

**Interfaces:**
- Produces: `SubjectDictionary` (bean) avec `String canonique(String libelle)` — renvoie le code canonique ou `null` si inconnu.

- [ ] **Step 1: Écrire le test (échoue)**

```java
// backend/src/test/java/bj/orientation/data/SubjectDictionaryTest.java
package bj.orientation.data;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SubjectDictionaryTest {
    private final SubjectDictionary dico = new SubjectDictionary();

    @Test
    void normaliseLesVariantesVersLeCanonique() {
        assertThat(dico.canonique("Maths")).isEqualTo("MATHS");
        assertThat(dico.canonique("Mathématiques")).isEqualTo("MATHS");
        assertThat(dico.canonique("PCT")).isEqualTo("PCT");
        assertThat(dico.canonique("SPCT")).isEqualTo("PCT");
        assertThat(dico.canonique("Sciences Physiques")).isEqualTo("PCT");
        assertThat(dico.canonique("SVT")).isEqualTo("SVT");
        assertThat(dico.canonique("Français")).isEqualTo("FR");
        assertThat(dico.canonique("Hist-Géo")).isEqualTo("HG");
        assertThat(dico.canonique("Anglais (LV1)")).isEqualTo("ANG");
        assertThat(dico.canonique("Philo")).isEqualTo("PHILO");
        assertThat(dico.canonique("matière inconnue xyz")).isNull();
    }
}
```

- [ ] **Step 2: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=SubjectDictionaryTest`
Expected: FAIL — classe absente.

- [ ] **Step 3: Implémenter `SubjectDictionary`**

```java
// backend/src/main/java/bj/orientation/data/SubjectDictionary.java
package bj.orientation.data;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SubjectDictionary {
    // clé = forme normalisée (sans accents, minuscule), valeur = code canonique
    private final Map<String, String> table = new LinkedHashMap<>();

    public SubjectDictionary() {
        put("MATHS", "maths", "mathematiques", "mathematique", "math");
        put("PCT", "pct", "spct", "sciences physiques", "physique chimie", "physique-chimie",
                "sciences physiques chimie et technologie");
        put("SVT", "svt", "sciences de la vie et de la terre");
        put("FR", "fr", "francais", "lettres");
        put("PHILO", "philo", "philosophie");
        put("HG", "hg", "hist-geo", "histoire-geographie", "histoire geographie", "hist geo");
        put("ANG", "ang", "anglais", "anglais lv1", "anglais (lv1)");
        put("ANG2", "anglais lv2", "anglais (lv2)");
        put("ESP", "esp", "espagnol", "espagnol (lv1)");
        put("ALL", "all", "allemand", "allemand (lv1)");
        put("ECO", "eco", "economie", "économie");
        put("EDC", "edc", "etude de cas", "etude de cas (g)");
        put("CG", "cg", "culture generale");
    }

    private void put(String canonique, String... formes) {
        for (String f : formes) table.put(normaliser(f), canonique);
    }

    public String canonique(String libelle) {
        if (libelle == null) return null;
        return table.get(normaliser(libelle));
    }

    static String normaliser(String s) {
        String sansAccent = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sansAccent.toLowerCase().replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ").trim();
    }
}
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=SubjectDictionaryTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/bj/orientation/data/SubjectDictionary.java backend/src/test/java/bj/orientation/data/SubjectDictionaryTest.java
git commit -m "feat(data): dictionnaire de synonymes des matières"
```

---

### Task 5: Éligibilité par série (SerieMatcher)

**Files:**
- Create: `backend/src/main/java/bj/orientation/data/SerieMatcher.java`
- Test: `backend/src/test/java/bj/orientation/data/SerieMatcherTest.java`

**Interfaces:**
- Produces: `SerieMatcher` (bean) avec `boolean accepte(String seriesBacRaw, String serieCode)`.

- [ ] **Step 1: Écrire le test (échoue)**

```java
// backend/src/test/java/bj/orientation/data/SerieMatcherTest.java
package bj.orientation.data;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SerieMatcherTest {
    private final SerieMatcher m = new SerieMatcher();

    @Test
    void matchLeCodeSerieCommeTokenExact() {
        assertThat(m.accepte("A1, A2, B, C, D, DEAT (toutes spécialités) et DT/STI", "C")).isTrue();
        assertThat(m.accepte("A1, A2, B, C, D", "D")).isTrue();
        assertThat(m.accepte("C, D", "A1")).isFalse();
    }

    @Test
    void neMatchePasUnCodeContenuDansUnAutreToken() {
        // "D" ne doit PAS matcher via "DEAT"
        assertThat(m.accepte("DEAT (toutes options)", "D")).isFalse();
    }

    @Test
    void matchDtEtDeatCommeFamilles() {
        assertThat(m.accepte("C, D, DT/IMI, DT/DWM", "DT")).isTrue();
        assertThat(m.accepte("C, D et DEAT/(toutes options)", "DEAT")).isTrue();
    }
}
```

- [ ] **Step 2: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=SerieMatcherTest`
Expected: FAIL — classe absente.

- [ ] **Step 3: Implémenter `SerieMatcher`**

```java
// backend/src/main/java/bj/orientation/data/SerieMatcher.java
package bj.orientation.data;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class SerieMatcher {

    public boolean accepte(String seriesBacRaw, String serieCode) {
        if (seriesBacRaw == null || serieCode == null) return false;
        Set<String> tokens = tokens(seriesBacRaw);
        String code = serieCode.trim().toUpperCase();
        if (tokens.contains(code)) return true;
        // familles DT / DEAT : présentes sous forme "DT/IMI", "DEAT/PV"
        if (code.equals("DT") && tokens.stream().anyMatch(t -> t.equals("DT"))) return true;
        if (code.equals("DEAT") && tokens.stream().anyMatch(t -> t.equals("DEAT"))) return true;
        return false;
    }

    private Set<String> tokens(String raw) {
        // découpe sur tout ce qui n'est pas alphanumérique → tokens exacts en majuscule
        String[] parts = raw.toUpperCase().split("[^A-Z0-9]+");
        return new HashSet<>(Arrays.asList(parts));
    }
}
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=SerieMatcherTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/bj/orientation/data/SerieMatcher.java backend/src/test/java/bj/orientation/data/SerieMatcherTest.java
git commit -m "feat(data): éligibilité filière par série (token exact)"
```

---

### Task 6: Résolution des matières (MatiereResolver)

**Files:**
- Create: `backend/src/main/java/bj/orientation/calc/MatiereResolver.java`
- Test: `backend/src/test/java/bj/orientation/calc/MatiereResolverTest.java`

**Interfaces:**
- Consumes: `SubjectDictionary`.
- Produces: `MatiereResolver` (bean) avec `List<String> resoudre(String matieresRaw, String serieCode)` — renvoie jusqu'à 3 codes canoniques, ou liste vide si non résoluble.

**Stratégie de parsing :** découper `matieres_raw` en clauses sur `|` ; choisir la clause dont le préfixe « Pour … : » cite la série (sinon la clause sans condition / la première) ; dans la clause, découper sur `/` et `,` ; pour chaque segment prendre le PREMIER libellé reconnu par le dictionnaire (gère « Maths ou Etude de cas (G) », « Hist-Géo/Anglais (DT/STI) ») ; garder au plus 3 codes distincts. Les mentions « toutes les trois matières écrites » (DEAT) → liste vide (données insuffisantes en V1).

- [ ] **Step 1: Écrire les tests (échoue)**

```java
// backend/src/test/java/bj/orientation/calc/MatiereResolverTest.java
package bj.orientation.calc;

import bj.orientation.data.SubjectDictionary;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MatiereResolverTest {
    private final MatiereResolver r = new MatiereResolver(new SubjectDictionary());

    @Test
    void casSimpleTroisMatieres() {
        assertThat(r.resoudre("Maths / PCT / SVT", "D"))
            .containsExactly("MATHS", "PCT", "SVT");
    }

    @Test
    void casConditionnelChoisitLaClauseDeLaSerie() {
        String raw = "Pour C et D: Maths, PCT, SVT | Pour DEAT: toutes les trois (03) matières écrites";
        assertThat(r.resoudre(raw, "C")).containsExactly("MATHS", "PCT", "SVT");
    }

    @Test
    void casDeatDonneListeVide() {
        String raw = "Pour C et D: Maths, PCT, SVT | Pour DEAT: toutes les trois (03) matières écrites";
        assertThat(r.resoudre(raw, "DEAT")).isEmpty();
    }

    @Test
    void casOuPrendLePremierReconnu() {
        assertThat(r.resoudre("Maths ou Etude de Cas (G) / Français / Anglais", "D"))
            .containsExactly("MATHS", "FR", "ANG");
    }
}
```

- [ ] **Step 2: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=MatiereResolverTest`
Expected: FAIL — classe absente.

- [ ] **Step 3: Implémenter `MatiereResolver`**

```java
// backend/src/main/java/bj/orientation/calc/MatiereResolver.java
package bj.orientation.calc;

import bj.orientation.data.SubjectDictionary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MatiereResolver {
    private final SubjectDictionary dico;

    public MatiereResolver(SubjectDictionary dico) {
        this.dico = dico;
    }

    public List<String> resoudre(String matieresRaw, String serieCode) {
        if (matieresRaw == null || matieresRaw.isBlank()) return List.of();
        String code = serieCode.trim().toUpperCase();
        String clause = choisirClause(matieresRaw, code);
        if (clause == null || clause.toLowerCase().contains("toutes les trois")
                || clause.toLowerCase().contains("non précisé")) {
            return List.of();
        }
        List<String> res = new ArrayList<>();
        for (String segment : clause.split("[/,]")) {
            String canon = premierReconnu(segment);
            if (canon != null && !res.contains(canon)) res.add(canon);
            if (res.size() == 3) break;
        }
        return res;
    }

    private String choisirClause(String raw, String code) {
        String[] clauses = raw.split("\\|");
        String defaut = null;
        for (String c : clauses) {
            String bas = c.toLowerCase();
            int idx = bas.indexOf(':');
            if (idx > 0 && bas.substring(0, idx).contains("pour")) {
                // clause conditionnelle : la série doit apparaître comme token dans le préfixe
                String prefixe = " " + bas.substring(0, idx).toUpperCase().replaceAll("[^A-Z0-9]", " ") + " ";
                if (prefixe.contains(" " + code + " ")) {
                    return c.substring(c.indexOf(':') + 1);
                }
            } else if (defaut == null) {
                defaut = c;
            }
        }
        return defaut != null ? defaut : (clauses.length > 0 ? clauses[0] : raw);
    }

    private String premierReconnu(String segment) {
        // essaie le segment entier, puis chaque sous-token séparé par "ou"
        String canon = dico.canonique(nettoyer(segment));
        if (canon != null) return canon;
        for (String part : segment.split("(?i)\\bou\\b")) {
            canon = dico.canonique(nettoyer(part));
            if (canon != null) return canon;
        }
        return null;
    }

    private String nettoyer(String s) {
        // retire les parenthèses conditionnelles "(...)"
        return s.replaceAll("\\([^)]*\\)", " ").trim();
    }
}
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=MatiereResolverTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/bj/orientation/calc/MatiereResolver.java backend/src/test/java/bj/orientation/calc/MatiereResolverTest.java
git commit -m "feat(calc): résolution des matières par série depuis matieres_raw"
```

---

### Task 7: Calcul de la moyenne pondérée (MoyenneCalculator)

**Files:**
- Create: `backend/src/main/java/bj/orientation/calc/MoyenneCalculator.java`
- Test: `backend/src/test/java/bj/orientation/calc/MoyenneCalculatorTest.java`

**Interfaces:**
- Consumes: `MatiereNote`.
- Produces: `MoyenneCalculator` (bean) avec `java.util.OptionalDouble calculer(List<String> matieresCanoniques, List<MatiereNote> notes)` — vide si une matière requise manque.

- [ ] **Step 1: Écrire le test golden (échoue)**

```java
// backend/src/test/java/bj/orientation/calc/MoyenneCalculatorTest.java
package bj.orientation.calc;

import bj.orientation.model.MatiereNote;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class MoyenneCalculatorTest {
    private final MoyenneCalculator calc = new MoyenneCalculator();

    @Test
    void goldenMedecineBacD_svt5_maths4_spct4() {
        // Guide: M = (SVT*5 + Maths*4 + PCT*4) / 13
        var notes = List.of(
            new MatiereNote("SVT", 15, 5),
            new MatiereNote("MATHS", 10, 4),
            new MatiereNote("PCT", 13, 4)
        );
        double attendu = (15*5 + 10*4 + 13*4) / 13.0;
        assertThat(calc.calculer(List.of("MATHS","PCT","SVT"), notes).getAsDouble())
            .isCloseTo(attendu, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void matiereManquanteRendVide() {
        var notes = List.of(new MatiereNote("MATHS", 12, 4));
        assertThat(calc.calculer(List.of("MATHS","PCT","SVT"), notes)).isEmpty();
    }
}
```

- [ ] **Step 2: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=MoyenneCalculatorTest`
Expected: FAIL — classe absente.

- [ ] **Step 3: Implémenter `MoyenneCalculator`**

```java
// backend/src/main/java/bj/orientation/calc/MoyenneCalculator.java
package bj.orientation.calc;

import bj.orientation.model.MatiereNote;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MoyenneCalculator {

    public OptionalDouble calculer(List<String> matieresCanoniques, List<MatiereNote> notes) {
        if (matieresCanoniques == null || matieresCanoniques.isEmpty()) return OptionalDouble.empty();
        Map<String, MatiereNote> parCode = notes.stream()
            .collect(Collectors.toMap(MatiereNote::canonique, Function.identity(), (a, b) -> a));
        double sommeNoteCoef = 0, sommeCoef = 0;
        for (String m : matieresCanoniques) {
            MatiereNote n = parCode.get(m);
            if (n == null || n.coefficient() <= 0) return OptionalDouble.empty();
            sommeNoteCoef += n.note() * n.coefficient();
            sommeCoef += n.coefficient();
        }
        return OptionalDouble.of(sommeNoteCoef / sommeCoef);
    }
}
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=MoyenneCalculatorTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/bj/orientation/calc/MoyenneCalculator.java backend/src/test/java/bj/orientation/calc/MoyenneCalculatorTest.java
git commit -m "feat(calc): moyenne pondérée (golden test Médecine Bac D)"
```

---

### Task 8: Paramètres externalisés (EstimateurProperties)

**Files:**
- Create: `backend/src/main/java/bj/orientation/config/EstimateurProperties.java`
- Modify: `backend/src/main/resources/application.yml`
- Modify: `backend/src/main/java/bj/orientation/OrientationApplication.java` (activer `@ConfigurationPropertiesScan`)
- Test: `backend/src/test/java/bj/orientation/config/EstimateurPropertiesTest.java`

**Interfaces:**
- Produces: `EstimateurProperties` avec `double sigma()`, `Map<Palier, Seuils> paliers()` (record `Seuils(double bourse, Double aide)`), `List<String> prestige()`.

- [ ] **Step 1: Écrire le test (échoue)**

```java
// backend/src/test/java/bj/orientation/config/EstimateurPropertiesTest.java
package bj.orientation.config;

import bj.orientation.model.Palier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EstimateurPropertiesTest {
    @Autowired EstimateurProperties props;

    @Test
    void chargeLesSeuilsDepuisApplicationYml() {
        assertThat(props.sigma()).isEqualTo(1.2);
        assertThat(props.paliers().get(Palier.T1).bourse()).isEqualTo(15.0);
        assertThat(props.paliers().get(Palier.T4).aide()).isEqualTo(10.0);
        assertThat(props.prestige()).contains("Médecine Générale");
    }
}
```

- [ ] **Step 2: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=EstimateurPropertiesTest`
Expected: FAIL — classe absente.

- [ ] **Step 3: Créer la classe et compléter la config**

```java
// backend/src/main/java/bj/orientation/config/EstimateurProperties.java
package bj.orientation.config;

import bj.orientation.model.Palier;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "estimateur")
public record EstimateurProperties(double sigma, Map<Palier, Seuils> paliers, List<String> prestige) {
    public record Seuils(double bourse, Double aide) {}
}
```

Ajouter à `application.yml` :

```yaml
estimateur:
  sigma: 1.2
  paliers:
    T1: { bourse: 15.0, aide: null }
    T2: { bourse: 13.0, aide: 11.5 }
    T3: { bourse: 11.5, aide: 10.5 }
    T4: { bourse: 10.5, aide: 10.0 }
  prestige:
    - "Médecine Générale"
    - "Médecine Humaine"
    - "Pharmacie"
    - "Kinésithérapie"
    - "Classes préparatoires MPSI (Mathématiques, Physiques et Science de l'Ingénieur) et PCSI (Physique-Chimie et Science de l'Ingénieur)"
    - "Sciences et Techniques de l'Ingénieur"
    - "Droit"
    - "Droit Privé"
    - "Droit Public"
    - "Sciences Economiques et de Gestion (Tronc commun)"
```

Activer le scan dans la classe principale :

```java
// OrientationApplication.java — remplacer l'annotation
package bj.orientation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OrientationApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrientationApplication.class, args);
    }
}
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=EstimateurPropertiesTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/bj/orientation/config backend/src/main/resources/application.yml backend/src/main/java/bj/orientation/OrientationApplication.java backend/src/test/java/bj/orientation/config
git commit -m "feat(config): paramètres externalisés de l'estimateur (seuils, sigma, prestige)"
```

---

### Task 9: Affectation du palier (SelectiviteTiering)

**Files:**
- Create: `backend/src/main/java/bj/orientation/calc/SelectiviteTiering.java`
- Test: `backend/src/test/java/bj/orientation/calc/SelectiviteTieringTest.java`

**Interfaces:**
- Consumes: `Filiere`, `EstimateurProperties`, `Palier`.
- Produces: `SelectiviteTiering` (bean) avec `Palier palier(Filiere f)`.

**Règle (spec §6) :** prestige → T1 si (bourse petit `<20` OU aide==0) sinon T2 ; sinon aide ≥ 3×bourse → T4 ; sinon aide>0 → T3 ; sinon T3.

- [ ] **Step 1: Écrire le test (échoue)**

```java
// backend/src/test/java/bj/orientation/calc/SelectiviteTieringTest.java
package bj.orientation.calc;

import bj.orientation.config.EstimateurProperties;
import bj.orientation.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class SelectiviteTieringTest {
    private final EstimateurProperties props = new EstimateurProperties(
        1.2,
        Map.of(Palier.T1, new EstimateurProperties.Seuils(15.0, null),
               Palier.T2, new EstimateurProperties.Seuils(13.0, 11.5),
               Palier.T3, new EstimateurProperties.Seuils(11.5, 10.5),
               Palier.T4, new EstimateurProperties.Seuils(10.5, 10.0)),
        List.of("Médecine Générale"));
    private final SelectiviteTiering t = new SelectiviteTiering(props);

    private Filiere f(String nom, int bourse, int aide) {
        return new Filiere(1, "U", "E", nom, bourse, aide, ModeEntree.CLASSEMENT, "C, D", "Maths / PCT / SVT", List.of(), 1);
    }

    @Test
    void prestigeSansAideEstT1() {
        assertThat(t.palier(f("Médecine Générale", 150, 0))).isEqualTo(Palier.T1);
    }

    @Test
    void grosCoussinAideEstT4() {
        assertThat(t.palier(f("Sciences Economiques", 207, 1407))).isEqualTo(Palier.T4);
    }

    @Test
    void aideModeréeEstT3() {
        assertThat(t.palier(f("Psychologie", 44, 60))).isEqualTo(Palier.T3);
    }

    @Test
    void sansAideNonPrestigeEstT3() {
        assertThat(t.palier(f("Andragogie", 8, 0))).isEqualTo(Palier.T3);
    }
}
```

- [ ] **Step 2: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=SelectiviteTieringTest`
Expected: FAIL — classe absente.

- [ ] **Step 3: Implémenter `SelectiviteTiering`**

```java
// backend/src/main/java/bj/orientation/calc/SelectiviteTiering.java
package bj.orientation.calc;

import bj.orientation.config.EstimateurProperties;
import bj.orientation.model.Filiere;
import bj.orientation.model.Palier;
import org.springframework.stereotype.Component;

@Component
public class SelectiviteTiering {
    private final EstimateurProperties props;

    public SelectiviteTiering(EstimateurProperties props) {
        this.props = props;
    }

    public Palier palier(Filiere f) {
        boolean prestige = props.prestige().contains(f.filiere());
        int bourse = f.quotaBourse();
        int aide = f.quotaAideFpp();
        if (prestige) {
            return (bourse < 20 || aide == 0) ? Palier.T1 : Palier.T2;
        }
        if (aide >= 3 * Math.max(bourse, 1)) return Palier.T4;
        if (aide > 0) return Palier.T3;
        return Palier.T3;
    }
}
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=SelectiviteTieringTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/bj/orientation/calc/SelectiviteTiering.java backend/src/test/java/bj/orientation/calc/SelectiviteTieringTest.java
git commit -m "feat(calc): affectation du palier de sélectivité"
```

---

### Task 10: Estimateur de probabilités (ProbabilityEstimator)

**Files:**
- Create: `backend/src/main/java/bj/orientation/model/Probabilites.java`
- Create: `backend/src/main/java/bj/orientation/calc/ProbabilityEstimator.java`
- Test: `backend/src/test/java/bj/orientation/calc/ProbabilityEstimatorTest.java`

**Interfaces:**
- Consumes: `Filiere`, `Palier`, `EstimateurProperties`, `SelectiviteTiering`, `StatutEstime`.
- Produces:
  - `record Probabilites(double pBourse, double pAide, double pPayant, StatutEstime statut, double pctAffiche)`.
  - `ProbabilityEstimator` (bean) avec `Probabilites estimer(Filiere f, double moyenne)`.

- [ ] **Step 1: Écrire les tests (échoue)**

```java
// backend/src/test/java/bj/orientation/calc/ProbabilityEstimatorTest.java
package bj.orientation.calc;

import bj.orientation.config.EstimateurProperties;
import bj.orientation.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class ProbabilityEstimatorTest {
    private final EstimateurProperties props = new EstimateurProperties(
        1.2,
        Map.of(Palier.T1, new EstimateurProperties.Seuils(15.0, null),
               Palier.T2, new EstimateurProperties.Seuils(13.0, 11.5),
               Palier.T3, new EstimateurProperties.Seuils(11.5, 10.5),
               Palier.T4, new EstimateurProperties.Seuils(10.5, 10.0)),
        List.of("Médecine Générale"));
    private final ProbabilityEstimator est =
        new ProbabilityEstimator(new SelectiviteTiering(props), props);

    private Filiere f(String nom, int bourse, int aide) {
        return new Filiere(1, "U", "E", nom, bourse, aide, ModeEntree.CLASSEMENT, "C, D", "Maths / PCT / SVT", List.of(), 1);
    }

    @Test
    void sommeDesProbasVautUn() {
        Probabilites p = est.estimer(f("Psychologie", 44, 60), 12.0);
        assertThat(p.pBourse() + p.pAide() + p.pPayant()).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void pBourseCroitAvecLaMoyenne() {
        Filiere med = f("Médecine Générale", 150, 0);
        assertThat(est.estimer(med, 17).pBourse()).isGreaterThan(est.estimer(med, 12).pBourse());
    }

    @Test
    void pAideNulleQuandAucunCoussin() {
        assertThat(est.estimer(f("Médecine Générale", 150, 0), 14).pAide()).isEqualTo(0.0);
    }

    @Test
    void pBourseEstBornee() {
        assertThat(est.estimer(f("Médecine Générale", 150, 0), 20).pBourse()).isLessThanOrEqualTo(0.98);
        assertThat(est.estimer(f("Médecine Générale", 150, 0), 2).pBourse()).isGreaterThanOrEqualTo(0.02);
    }
}
```

- [ ] **Step 2: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=ProbabilityEstimatorTest`
Expected: FAIL — classes absentes.

- [ ] **Step 3: Implémenter le record et l'estimateur**

```java
// backend/src/main/java/bj/orientation/model/Probabilites.java
package bj.orientation.model;
public record Probabilites(double pBourse, double pAide, double pPayant,
                           StatutEstime statut, double pctAffiche) {}
```

```java
// backend/src/main/java/bj/orientation/calc/ProbabilityEstimator.java
package bj.orientation.calc;

import bj.orientation.config.EstimateurProperties;
import bj.orientation.model.Filiere;
import bj.orientation.model.Palier;
import bj.orientation.model.Probabilites;
import bj.orientation.model.StatutEstime;
import org.springframework.stereotype.Component;

@Component
public class ProbabilityEstimator {
    private final SelectiviteTiering tiering;
    private final EstimateurProperties props;

    public ProbabilityEstimator(SelectiviteTiering tiering, EstimateurProperties props) {
        this.tiering = tiering;
        this.props = props;
    }

    public Probabilites estimer(Filiere f, double moyenne) {
        Palier palier = tiering.palier(f);
        EstimateurProperties.Seuils seuils = props.paliers().get(palier);
        double sigma = props.sigma();

        double pBourse = clamp(logistique((moyenne - seuils.bourse()) / sigma), 0.02, 0.98);

        double pAide = 0.0;
        if (f.quotaAideFpp() > 0) {
            double seuilAide = seuils.aide() != null ? seuils.aide() : seuils.bourse() - 1.0;
            pAide = (1 - pBourse) * logistique((moyenne - seuilAide) / sigma);
        }
        double pPayant = Math.max(0.0, 1 - pBourse - pAide);

        StatutEstime statut;
        double pct;
        if (pBourse >= pAide && pBourse >= pPayant) { statut = StatutEstime.BOURSIER; pct = pBourse; }
        else if (pAide >= pPayant) { statut = StatutEstime.AIDE; pct = pAide; }
        else { statut = StatutEstime.PAYANT; pct = pPayant; }

        return new Probabilites(pBourse, pAide, pPayant, statut, pct);
    }

    private static double logistique(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=ProbabilityEstimatorTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/bj/orientation/model/Probabilites.java backend/src/main/java/bj/orientation/calc/ProbabilityEstimator.java backend/src/test/java/bj/orientation/calc/ProbabilityEstimatorTest.java
git commit -m "feat(calc): estimateur de probabilités logistique par palier"
```

---

### Task 11: Argumentaire (ArgumentaireBuilder)

**Files:**
- Create: `backend/src/main/java/bj/orientation/calc/ArgumentaireBuilder.java`
- Test: `backend/src/test/java/bj/orientation/calc/ArgumentaireBuilderTest.java`

**Interfaces:**
- Consumes: `Filiere`, `Probabilites`, `StatutEstime`.
- Produces: `ArgumentaireBuilder` (bean) avec `String construire(Filiere f, double moyenne, Probabilites p)`.

**Constante :** `String DISCLAIMER = "Estimation indicative, pas une garantie de sélection."` toujours en fin de texte.

- [ ] **Step 1: Écrire les tests (échoue)**

```java
// backend/src/test/java/bj/orientation/calc/ArgumentaireBuilderTest.java
package bj.orientation.calc;

import bj.orientation.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ArgumentaireBuilderTest {
    private final ArgumentaireBuilder b = new ArgumentaireBuilder();

    private Filiere f(int bourse, int aide) {
        return new Filiere(1, "UAC", "FSS", "Médecine Générale", bourse, aide,
            ModeEntree.CLASSEMENT, "C, D", "Maths / PCT / SVT", List.of("Médecin généraliste"), 31);
    }

    @Test
    void argumentaireBoursierContientLaFiliereEtLeDisclaimer() {
        var p = new Probabilites(0.8, 0.0, 0.2, StatutEstime.BOURSIER, 0.8);
        String txt = b.construire(f(150, 0), 16.0, p);
        assertThat(txt).contains("Médecine Générale").contains("FSS");
        assertThat(txt).endsWith(ArgumentaireBuilder.DISCLAIMER);
    }

    @Test
    void argumentaireAideMentionneLesPlacesDAide() {
        var p = new Probabilites(0.2, 0.6, 0.2, StatutEstime.AIDE, 0.6);
        String txt = b.construire(f(60, 340), 11.5, p);
        assertThat(txt).contains("340");
    }
}
```

- [ ] **Step 2: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=ArgumentaireBuilderTest`
Expected: FAIL — classe absente.

- [ ] **Step 3: Implémenter `ArgumentaireBuilder`**

```java
// backend/src/main/java/bj/orientation/calc/ArgumentaireBuilder.java
package bj.orientation.calc;

import bj.orientation.model.Filiere;
import bj.orientation.model.Probabilites;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ArgumentaireBuilder {
    public static final String DISCLAIMER = "Estimation indicative, pas une garantie de sélection.";

    public String construire(Filiere f, double moyenne, Probabilites p) {
        String m = String.format(Locale.FRANCE, "%.2f", moyenne);
        String pct = String.format(Locale.FRANCE, "%.0f%%", p.pctAffiche() * 100);
        String base = switch (p.statut()) {
            case BOURSIER -> "Avec une moyenne estimée de " + m + "/20, tu es bien placé(e) pour une BOURSE en «"
                    + f.filiere() + "» à " + f.etablissement() + " (" + f.quotaBourse() + " bourses). Chance estimée : " + pct + ".";
            case AIDE -> "Ta moyenne estimée de " + m + "/20 te situe plutôt sur une place d'AIDE/FPP en «"
                    + f.filiere() + "» (" + f.quotaAideFpp() + " places d'aide). Chance estimée : " + pct + ".";
            case PAYANT, PAYANT_UNIQUEMENT -> "Avec " + m + "/20, une sélection en boursier/aide est peu probable en «"
                    + f.filiere() + "» ; regarde les alternatives à meilleures cotes.";
            case CONCOURS -> "«" + f.filiere() + "» recrute par CONCOURS, pas au classement : prépare l'épreuve.";
        };
        String debouches = f.debouches().isEmpty() ? "" : " Débouchés : " + String.join(", ",
                f.debouches().subList(0, Math.min(2, f.debouches().size()))) + ".";
        return base + debouches + " " + DISCLAIMER;
    }
}
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=ArgumentaireBuilderTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/bj/orientation/calc/ArgumentaireBuilder.java backend/src/test/java/bj/orientation/calc/ArgumentaireBuilderTest.java
git commit -m "feat(calc): argumentaire rule-based avec disclaimer"
```

---

### Task 12: Orchestration (Recommender) + records de réponse

**Files:**
- Create: `backend/src/main/java/bj/orientation/model/Recommandation.java`
- Create: `backend/src/main/java/bj/orientation/model/RecommandationRequest.java`
- Create: `backend/src/main/java/bj/orientation/model/RecommandationResponse.java`
- Create: `backend/src/main/java/bj/orientation/calc/Recommender.java`
- Test: `backend/src/test/java/bj/orientation/calc/RecommenderTest.java`

**Interfaces:**
- Consumes: `FiliereRepository`, `SerieMatcher`, `SubjectDictionary`, `MatiereResolver`, `MoyenneCalculator`, `ProbabilityEstimator`, `ArgumentaireBuilder`.
- Produces:
  - `record Recommandation(Filiere filiere, double moyenne, Probabilites proba, String argumentaire)`.
  - `record RecommandationRequest(String serie, List<NoteSaisie> notes, String domaine)`.
  - `record RecommandationResponse(List<Recommandation> top3, List<Recommandation> alternatives, List<Filiere> concours, List<Filiere> payantes, List<Filiere> donneesInsuffisantes)`.
  - `Recommender` (bean) avec `RecommandationResponse recommander(RecommandationRequest req)`.

**Logique :** normaliser les notes (via `SubjectDictionary`) ; pour chaque filière éligible à la série : si CONCOURS → liste concours ; si PAYANT → liste payantes ; sinon résoudre matières + calculer moyenne : si vide → donneesInsuffisantes, sinon estimer proba, calculer `score = pBourse + 0.5*pAide`, construire l'argumentaire. Trier par score décroissant ; top3 = 3 premiers, alternatives = 5 suivants. (Filtre `domaine` = V2 ; le champ est accepté mais ignoré ici — documenté.)

- [ ] **Step 1: Écrire le test (échoue)**

```java
// backend/src/test/java/bj/orientation/calc/RecommenderTest.java
package bj.orientation.calc;

import bj.orientation.config.EstimateurProperties;
import bj.orientation.data.*;
import bj.orientation.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class RecommenderTest {
    private Recommender build() {
        var props = new EstimateurProperties(1.2,
            Map.of(Palier.T1, new EstimateurProperties.Seuils(15.0, null),
                   Palier.T2, new EstimateurProperties.Seuils(13.0, 11.5),
                   Palier.T3, new EstimateurProperties.Seuils(11.5, 10.5),
                   Palier.T4, new EstimateurProperties.Seuils(10.5, 10.0)),
            List.of("Médecine Générale"));
        var dico = new SubjectDictionary();
        return new Recommender(new FiliereRepository(), new SerieMatcher(), dico,
            new MatiereResolver(dico), new MoyenneCalculator(),
            new ProbabilityEstimator(new SelectiviteTiering(props), props),
            new ArgumentaireBuilder());
    }

    @Test
    void eleveDFortRecoitTop3NonVide() {
        var req = new RecommandationRequest("D", List.of(
            new NoteSaisie("Maths", 16, 4),
            new NoteSaisie("PCT", 15, 4),
            new NoteSaisie("SVT", 17, 5)
        ), null);
        var resp = build().recommander(req);
        assertThat(resp.top3()).isNotEmpty().hasSizeLessThanOrEqualTo(3);
        // top3 trié par score décroissant
        var scores = resp.top3().stream()
            .map(r -> r.proba().pBourse() + 0.5 * r.proba().pAide()).toList();
        assertThat(scores).isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Test
    void filieresConcoursSontListeesAPart() {
        var req = new RecommandationRequest("D", List.of(
            new NoteSaisie("PCT", 14, 4), new NoteSaisie("SVT", 14, 5), new NoteSaisie("Maths", 12, 4)), null);
        var resp = build().recommander(req);
        assertThat(resp.concours()).anyMatch(f -> f.modeEntree() == ModeEntree.CONCOURS);
    }
}
```

- [ ] **Step 2: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=RecommenderTest`
Expected: FAIL — classes absentes.

- [ ] **Step 3: Créer les records puis `Recommender`**

```java
// Recommandation.java
package bj.orientation.model;
public record Recommandation(Filiere filiere, double moyenne, Probabilites proba, String argumentaire) {}
```

```java
// RecommandationRequest.java
package bj.orientation.model;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
public record RecommandationRequest(@NotBlank String serie, @NotEmpty List<NoteSaisie> notes, String domaine) {}
```

```java
// RecommandationResponse.java
package bj.orientation.model;
import java.util.List;
public record RecommandationResponse(
        List<Recommandation> top3, List<Recommandation> alternatives,
        List<Filiere> concours, List<Filiere> payantes, List<Filiere> donneesInsuffisantes) {}
```

```java
// backend/src/main/java/bj/orientation/calc/Recommender.java
package bj.orientation.calc;

import bj.orientation.data.FiliereRepository;
import bj.orientation.data.SerieMatcher;
import bj.orientation.data.SubjectDictionary;
import bj.orientation.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;

@Component
public class Recommender {
    private final FiliereRepository repo;
    private final SerieMatcher serieMatcher;
    private final SubjectDictionary dico;
    private final MatiereResolver resolver;
    private final MoyenneCalculator calculator;
    private final ProbabilityEstimator estimator;
    private final ArgumentaireBuilder argumentaire;

    public Recommender(FiliereRepository repo, SerieMatcher serieMatcher, SubjectDictionary dico,
                       MatiereResolver resolver, MoyenneCalculator calculator,
                       ProbabilityEstimator estimator, ArgumentaireBuilder argumentaire) {
        this.repo = repo; this.serieMatcher = serieMatcher; this.dico = dico;
        this.resolver = resolver; this.calculator = calculator;
        this.estimator = estimator; this.argumentaire = argumentaire;
    }

    public RecommandationResponse recommander(RecommandationRequest req) {
        List<MatiereNote> notes = normaliser(req.notes());
        List<Recommandation> scorables = new ArrayList<>();
        List<Filiere> concours = new ArrayList<>();
        List<Filiere> payantes = new ArrayList<>();
        List<Filiere> insuffisantes = new ArrayList<>();

        for (Filiere f : repo.toutes()) {
            if (!serieMatcher.accepte(f.seriesBacRaw(), req.serie())) continue;
            switch (f.modeEntree()) {
                case CONCOURS -> concours.add(f);
                case PAYANT -> payantes.add(f);
                case CLASSEMENT -> {
                    List<String> matieres = resolver.resoudre(f.matieresRaw(), req.serie());
                    OptionalDouble moyenne = calculator.calculer(matieres, notes);
                    if (moyenne.isEmpty()) { insuffisantes.add(f); break; }
                    Probabilites p = estimator.estimer(f, moyenne.getAsDouble());
                    String arg = argumentaire.construire(f, moyenne.getAsDouble(), p);
                    scorables.add(new Recommandation(f, moyenne.getAsDouble(), p, arg));
                }
            }
        }

        scorables.sort(Comparator.comparingDouble(this::score).reversed());
        List<Recommandation> top3 = scorables.subList(0, Math.min(3, scorables.size()));
        List<Recommandation> alternatives = scorables.subList(
                Math.min(3, scorables.size()), Math.min(8, scorables.size()));

        return new RecommandationResponse(new ArrayList<>(top3), new ArrayList<>(alternatives),
                concours, payantes, insuffisantes);
    }

    private double score(Recommandation r) {
        return r.proba().pBourse() + 0.5 * r.proba().pAide();
    }

    private List<MatiereNote> normaliser(List<NoteSaisie> saisies) {
        List<MatiereNote> res = new ArrayList<>();
        for (NoteSaisie s : saisies) {
            String canon = dico.canonique(s.libelle());
            if (canon != null) res.add(new MatiereNote(canon, s.note(), s.coefficient()));
        }
        return res;
    }
}
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=RecommenderTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/bj/orientation/model/Recommandation.java backend/src/main/java/bj/orientation/model/RecommandationRequest.java backend/src/main/java/bj/orientation/model/RecommandationResponse.java backend/src/main/java/bj/orientation/calc/Recommender.java backend/src/test/java/bj/orientation/calc/RecommenderTest.java
git commit -m "feat(calc): orchestration et classement hybride (Recommender)"
```

---

### Task 13: API REST + validation

**Files:**
- Create: `backend/src/main/java/bj/orientation/web/MetaController.java`
- Create: `backend/src/main/java/bj/orientation/web/RecommandationController.java`
- Create: `backend/src/main/java/bj/orientation/web/GlobalExceptionHandler.java`
- Test: `backend/src/test/java/bj/orientation/web/RecommandationControllerTest.java`

**Interfaces:**
- Consumes: `Recommender`, `Serie`.
- Produces: endpoints `GET /api/series`, `POST /api/recommander`.

- [ ] **Step 1: Écrire le test d'intégration MockMvc (échoue)**

```java
// backend/src/test/java/bj/orientation/web/RecommandationControllerTest.java
package bj.orientation.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RecommandationControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void recommanderRetourneUnTop3PourUnEleveD() throws Exception {
        String body = """
            {"serie":"D","notes":[
              {"libelle":"Maths","note":16,"coefficient":4},
              {"libelle":"PCT","note":15,"coefficient":4},
              {"libelle":"SVT","note":17,"coefficient":5}
            ]}""";
        mvc.perform(post("/api/recommander").contentType("application/json").content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.top3").isArray())
           .andExpect(jsonPath("$.top3[0].filiere.filiere").exists());
    }

    @Test
    void requeteInvalideRetourne400() throws Exception {
        String body = "{\"serie\":\"\",\"notes\":[]}";
        mvc.perform(post("/api/recommander").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }

    @Test
    void seriesRetourneLaListe() throws Exception {
        mvc.perform(get("/api/series"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0]").exists());
    }
}
```

- [ ] **Step 2: Lancer le test (doit échouer)**

Run: `cd backend && mvn -q test -Dtest=RecommandationControllerTest`
Expected: FAIL — contrôleurs absents.

- [ ] **Step 3: Implémenter les contrôleurs et le handler**

```java
// backend/src/main/java/bj/orientation/web/MetaController.java
package bj.orientation.web;

import bj.orientation.model.Serie;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MetaController {
    @GetMapping("/series")
    public List<String> series() {
        return Arrays.stream(Serie.values()).map(Enum::name).toList();
    }
}
```

```java
// backend/src/main/java/bj/orientation/web/RecommandationController.java
package bj.orientation.web;

import bj.orientation.calc.Recommender;
import bj.orientation.model.RecommandationRequest;
import bj.orientation.model.RecommandationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class RecommandationController {
    private final Recommender recommender;

    public RecommandationController(Recommender recommender) {
        this.recommender = recommender;
    }

    @PostMapping("/recommander")
    public RecommandationResponse recommander(@Valid @RequestBody RecommandationRequest req) {
        return recommender.recommander(req);
    }
}
```

```java
// backend/src/main/java/bj/orientation/web/GlobalExceptionHandler.java
package bj.orientation.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> onValidation(MethodArgumentNotValidException e) {
        return Map.of("erreur", "Requête invalide : série et notes obligatoires.");
    }
}
```

- [ ] **Step 4: Lancer le test (doit passer)**

Run: `cd backend && mvn -q test -Dtest=RecommandationControllerTest`
Expected: PASS.

- [ ] **Step 5: Vérifier la suite complète**

Run: `cd backend && mvn -q test`
Expected: BUILD SUCCESS, tous les tests verts.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/bj/orientation/web backend/src/test/java/bj/orientation/web
git commit -m "feat(web): API REST /api/series et /api/recommander + validation 400"
```

---

## Self-Review

**Spec coverage** (spec §) :
- §3 parcours (série+notes→top3) → Tasks 12-13. Filtre domaine = accepté mais V2 (documenté Task 12). ✅
- §4 architecture/modules → un module = une classe (Tasks 3-13). Concours/payant listés à part → Task 12. ✅
- §5 résolution matières+coefficients (relevé) → SubjectDictionary (4), MatiereResolver (6), MoyenneCalculator (7 golden). ✅
- §6 estimateur par palier + logistique + score → Tasks 8-10, 12. ✅
- §7 argumentaire + disclaimer → Task 11. ✅
- §8 OCR → **plan séparé (Plan 2)**. ✅ (hors périmètre de ce plan)
- §9 stats/APDP → **plan séparé (Plan 4)** ; ce plan ne persiste rien. ✅
- §10 cas limites : note hors 0-20 → à ajouter (voir ci-dessous) ; matière manquante → Task 7/12 ; concours/payant → Task 12. ⚠️
- §11 tests → chaque task en TDD, golden Task 7, intégration Task 13. ✅

**Correctif ajouté (couverture §10 — validation note 0-20)** : ajouter à `NoteSaisie` les contraintes `@DecimalMin("0")` `@DecimalMax("20")` et `@Valid` sur la liste, sinon une note aberrante fausse le calcul.

- Modifier `NoteSaisie.java` :
```java
package bj.orientation.model;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
public record NoteSaisie(String libelle,
        @DecimalMin("0") @DecimalMax("20") double note,
        @DecimalMin("0") double coefficient) {}
```
- Modifier `RecommandationRequest.java` : annoter la liste avec `@Valid` :
```java
public record RecommandationRequest(@NotBlank String serie,
        @NotEmpty @jakarta.validation.Valid List<NoteSaisie> notes, String domaine) {}
```
- Ajouter à `RecommandationControllerTest` un test : note=25 → 400. (Étape TDD à insérer en fin de Task 13.)

**Placeholder scan** : aucun TODO/TBD ; tout step de code montre le code. ✅
**Type consistency** : `Probabilites`, `Filiere`, `MatiereNote`, `RecommandationResponse` utilisés à l'identique entre tasks ; méthodes `resoudre/calculer/estimer/palier/construire/recommander/accepte/canonique` cohérentes. ✅

## Sous-plans suivants (hors de ce plan)
- **Plan 2 — OCR** : endpoint `POST /api/ocr` avec Tess4J (traineddata `fra`), parsing en tableau éditable, fichier supprimé après extraction.
- **Plan 3 — Frontend React** : formulaire série+notes (upload/manuel), tableau éditable, écran résultats (top-3 + statut + % + argumentaire), bandeau confidentialité.
- **Plan 4 — Stats MySQL** : `StatsService` + schéma agrégats anonymes uniquement.
