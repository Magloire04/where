# OCR du relevé (Tess4J) — Implementation Plan (Plan 2/4)

> **For agentic workers:** exécuter tâche par tâche en TDD (checkbox `- [ ]`). Sous-skill : superpowers:executing-plans.

**Goal:** Exposer `POST /api/v1/releves` qui reçoit une image/PDF de relevé, en extrait le texte par OCR local (Tesseract via Tess4J), et renvoie une liste de lignes (matière, note, coefficient) à corriger côté client. Aucune donnée conservée.

**Architecture:** L'OCR est isolé derrière une interface `OcrEngine` (impl Tesseract). Le vrai travail testable est le `ReleveParser` (texte OCR → lignes), unité pure sans dépendance native. Le contrôleur orchestre engine → parser et renvoie l'enveloppe ASIN. Les tests CI n'invoquent jamais le natif (moteur simulé).

**Tech Stack:** Spring Boot 3.3, Tess4J 5.x (Tesseract), JUnit 5, MockMvc.

## Global Constraints
- Enveloppe ASIN `{data}` / `{error}` ; route ressource au pluriel `/api/v1/releves`.
- **Aucun fichier conservé** : traitement en mémoire, rien sur disque ni en base.
- OCR indisponible/échec → réponse d'erreur claire invitant à la **saisie manuelle** (pas de 500 opaque).
- CI reste verte sans binaire natif : la couche native n'est jamais chargée dans les tests (moteur simulé via `@MockBean`).
- La donnée `fra.traineddata` (tessdata) est fournie hors-jar via un chemin configurable `ocr.tessdata-path` ; non committée.

## File Structure
```
backend/src/main/java/bj/orientation/
  model/LigneReleve.java              # record (libelle, note, coefficient) — nullable
  ocr/OcrEngine.java                  # interface
  ocr/OcrIndisponibleException.java   # exception métier
  ocr/TesseractOcrEngine.java         # impl Tess4J (native, non testée en CI)
  ocr/ReleveParser.java               # heuristique texte -> lignes (cœur testable)
  web/ReleveController.java           # POST /api/v1/releves (multipart)
  web/GlobalExceptionHandler.java     # + handler OcrIndisponibleException (503)
```

---

### Task 1: Dépendance Tess4J + propriété tessdata

**Files:** Modify `backend/pom.xml` ; Modify `backend/src/main/resources/application.yml`

- [ ] **Step 1: Ajouter la dépendance** dans `<dependencies>` du pom :
```xml
        <dependency>
            <groupId>net.sourceforge.tess4j</groupId>
            <artifactId>tess4j</artifactId>
            <version>5.13.0</version>
        </dependency>
```
- [ ] **Step 2: Ajouter la config** à `application.yml` :
```yaml
ocr:
  tessdata-path: ${OCR_TESSDATA_PATH:}
  langue: fra
```
- [ ] **Step 3: Vérifier la compilation** : `mvn -B -q compile` → BUILD SUCCESS (téléchargement Tess4J).
- [ ] **Step 4: Commit** : `chore(ocr): dependance tess4j et config tessdata`

---

### Task 2: Modèle LigneReleve + interface OcrEngine

**Files:** Create `model/LigneReleve.java`, `ocr/OcrEngine.java`, `ocr/OcrIndisponibleException.java`

**Interfaces produced:**
- `record LigneReleve(String libelle, Double note, Double coefficient)` (note/coef nullable si non lus).
- `interface OcrEngine { String extraireTexte(byte[] contenu, String nomFichier); }`
- `class OcrIndisponibleException extends RuntimeException`

- [ ] **Step 1: Écrire les 3 fichiers**
```java
// model/LigneReleve.java
package bj.orientation.model;
public record LigneReleve(String libelle, Double note, Double coefficient) {}
```
```java
// ocr/OcrEngine.java
package bj.orientation.ocr;
public interface OcrEngine {
    String extraireTexte(byte[] contenu, String nomFichier);
}
```
```java
// ocr/OcrIndisponibleException.java
package bj.orientation.ocr;
public class OcrIndisponibleException extends RuntimeException {
    public OcrIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
```
- [ ] **Step 2: Compiler** : `mvn -B -q compile` → SUCCESS.
- [ ] **Step 3: Commit** : `feat(ocr): modele ligne releve et interface moteur ocr`

---

### Task 3: ReleveParser (heuristique) — cœur testable

**Files:** Create `ocr/ReleveParser.java` ; Test `ocr/ReleveParserTest.java`

**Interface produced:** `@Component ReleveParser` avec `List<LigneReleve> parser(String texteOcr)`.

**Règles :** pour chaque ligne non vide : le **libellé** = texte avant le premier nombre ; on extrait les nombres (virgule → point) ; la **note** = premier nombre dans [0, 20] ; le **coefficient** = premier entier de [1, 10] situé après la note (sinon null). Une ligne sans libellé alphabétique OU sans note est ignorée.

- [ ] **Step 1: Écrire les tests (échoue)**
```java
// ocr/ReleveParserTest.java
package bj.orientation.ocr;

import bj.orientation.model.LigneReleve;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReleveParserTest {
    private final ReleveParser parser = new ReleveParser();

    @Test
    void litLibelleNoteEtCoefficient() {
        var lignes = parser.parser("Mathématiques 12,50 4");
        assertThat(lignes).hasSize(1);
        assertThat(lignes.get(0).libelle()).isEqualTo("Mathématiques");
        assertThat(lignes.get(0).note()).isEqualTo(12.5);
        assertThat(lignes.get(0).coefficient()).isEqualTo(4.0);
    }

    @Test
    void ignoreLesLignesSansNote() {
        assertThat(parser.parser("RELEVE DE NOTES\nEtablissement: Lycee")).isEmpty();
    }

    @Test
    void gereLePointDecimalEtPlusieursLignes() {
        var lignes = parser.parser("Physique-Chimie 09.00 4\nAnglais 14 2");
        assertThat(lignes).hasSize(2);
        assertThat(lignes.get(0).note()).isEqualTo(9.0);
        assertThat(lignes.get(1).coefficient()).isEqualTo(2.0);
    }
}
```
- [ ] **Step 2: Lancer** : `mvn -B test -Dtest=ReleveParserTest` → FAIL (classe absente).
- [ ] **Step 3: Implémenter `ReleveParser`**
```java
// ocr/ReleveParser.java
package bj.orientation.ocr;

import bj.orientation.model.LigneReleve;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ReleveParser {
    private static final Pattern NOMBRE = Pattern.compile("\\d+(?:[.,]\\d+)?");

    public List<LigneReleve> parser(String texteOcr) {
        List<LigneReleve> lignes = new ArrayList<>();
        if (texteOcr == null || texteOcr.isBlank()) {
            return lignes;
        }
        for (String ligne : texteOcr.split("\\r?\\n")) {
            LigneReleve extraite = parserLigne(ligne);
            if (extraite != null) {
                lignes.add(extraite);
            }
        }
        return lignes;
    }

    private LigneReleve parserLigne(String ligne) {
        Matcher matcher = NOMBRE.matcher(ligne);
        List<Double> nombres = new ArrayList<>();
        int premierIndex = -1;
        while (matcher.find()) {
            if (premierIndex < 0) {
                premierIndex = matcher.start();
            }
            nombres.add(Double.parseDouble(matcher.group().replace(',', '.')));
        }
        if (premierIndex <= 0) {
            return null;
        }
        String libelle = ligne.substring(0, premierIndex).trim();
        if (libelle.isEmpty() || !libelle.matches(".*[A-Za-zÀ-ÿ].*")) {
            return null;
        }
        Double note = null;
        Double coefficient = null;
        for (Double nombre : nombres) {
            if (note == null && nombre >= 0 && nombre <= 20) {
                note = nombre;
            } else if (note != null && coefficient == null
                    && nombre >= 1 && nombre <= 10 && nombre == Math.floor(nombre)) {
                coefficient = nombre;
            }
        }
        return note == null ? null : new LigneReleve(libelle, note, coefficient);
    }
}
```
- [ ] **Step 4: Lancer** : `mvn -B test -Dtest=ReleveParserTest` → PASS.
- [ ] **Step 5: Commit** : `feat(ocr): parser heuristique du releve`

---

### Task 4: TesseractOcrEngine (impl Tess4J)

**Files:** Create `ocr/TesseractOcrEngine.java`

**Interface produced:** `@Component TesseractOcrEngine implements OcrEngine`. Ne charge pas le natif au démarrage (seulement à l'appel `extraireTexte`).

- [ ] **Step 1: Implémenter**
```java
// ocr/TesseractOcrEngine.java
package bj.orientation.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

@Component
public class TesseractOcrEngine implements OcrEngine {

    private final String tessdataPath;
    private final String langue;

    public TesseractOcrEngine(
            @Value("${ocr.tessdata-path:}") String tessdataPath,
            @Value("${ocr.langue:fra}") String langue) {
        this.tessdataPath = tessdataPath;
        this.langue = langue;
    }

    @Override
    public String extraireTexte(byte[] contenu, String nomFichier) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(contenu));
            if (image == null) {
                throw new OcrIndisponibleException("Format image non lisible : " + nomFichier, null);
            }
            Tesseract tesseract = new Tesseract();
            if (tessdataPath != null && !tessdataPath.isBlank()) {
                tesseract.setDatapath(tessdataPath);
            }
            tesseract.setLanguage(langue);
            return tesseract.doOCR(image);
        } catch (TesseractException | java.io.IOException | UnsatisfiedLinkError | NoClassDefFoundError e) {
            throw new OcrIndisponibleException(
                "OCR indisponible sur ce serveur : saisissez vos notes manuellement.", e);
        }
    }
}
```
- [ ] **Step 2: Compiler** : `mvn -B -q compile` → SUCCESS.
- [ ] **Step 3: Commit** : `feat(ocr): moteur tesseract (tess4j) isole du reste`

---

### Task 5: Endpoint POST /api/v1/releves + gestion d'erreur

**Files:** Create `web/ReleveController.java` ; Modify `web/GlobalExceptionHandler.java` ; Test `web/ReleveControllerTest.java`

**Interface produced:** `POST /api/v1/releves` (multipart `fichier`) → `ApiResponse<List<LigneReleve>>`.

- [ ] **Step 1: Écrire le test (stub OcrEngine via @MockBean) — échoue**
```java
// web/ReleveControllerTest.java
package bj.orientation.web;

import bj.orientation.ocr.OcrEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReleveControllerTest {
    @Autowired MockMvc mvc;
    @MockBean OcrEngine ocrEngine;

    @Test
    void extraitLesLignesDuReleve() throws Exception {
        when(ocrEngine.extraireTexte(any(), any())).thenReturn("Mathématiques 12,50 4");
        var fichier = new MockMultipartFile("fichier", "releve.png", "image/png", new byte[]{1, 2, 3});
        mvc.perform(multipart("/api/v1/releves").file(fichier))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data[0].libelle").value("Mathématiques"))
           .andExpect(jsonPath("$.data[0].note").value(12.5));
    }
}
```
- [ ] **Step 2: Lancer** : `mvn -B test -Dtest=ReleveControllerTest` → FAIL.
- [ ] **Step 3: Implémenter le contrôleur**
```java
// web/ReleveController.java
package bj.orientation.web;

import bj.orientation.model.LigneReleve;
import bj.orientation.ocr.OcrEngine;
import bj.orientation.ocr.ReleveParser;
import bj.orientation.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** Extraction OCR d'un relevé (le fichier n'est jamais conservé). */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class ReleveController {
    private final OcrEngine ocrEngine;
    private final ReleveParser parser;

    public ReleveController(OcrEngine ocrEngine, ReleveParser parser) {
        this.ocrEngine = ocrEngine;
        this.parser = parser;
    }

    @PostMapping("/releves")
    public ApiResponse<List<LigneReleve>> extraire(@RequestParam("fichier") MultipartFile fichier)
            throws IOException {
        String texte = ocrEngine.extraireTexte(fichier.getBytes(), fichier.getOriginalFilename());
        return new ApiResponse<>(parser.parser(texte));
    }
}
```
- [ ] **Step 4: Ajouter le handler** dans `GlobalExceptionHandler` :
```java
    @org.springframework.web.bind.annotation.ExceptionHandler(bj.orientation.ocr.OcrIndisponibleException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
    public bj.orientation.web.dto.ApiErrorResponse onOcrIndisponible(bj.orientation.ocr.OcrIndisponibleException e) {
        return bj.orientation.web.dto.ApiErrorResponse.of(
            "OCR_INDISPONIBLE", e.getMessage(), org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE.value());
    }
```
- [ ] **Step 5: Lancer** : `mvn -B test` → tous verts.
- [ ] **Step 6: Commit** : `feat(ocr): endpoint post /api/v1/releves + gestion erreur`

---

## Self-Review
- Spec §8 (OCR upload → tableau éditable, fichier supprimé, repli manuel) → Tasks 3-5. ✅
- Enveloppe ASIN + route pluriel → Task 5. ✅
- CI sans natif → moteur simulé (@MockBean), parser testé sans natif. ✅
- Placeholders : aucun. Types (`LigneReleve`, `OcrEngine`) cohérents entre tasks. ✅
- Note : la validation OCR réelle (Tesseract natif + `fra.traineddata`) est **manuelle/hors CI** — documentée dans `backend/README.md`.
