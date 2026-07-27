package bj.orientation.ocr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implémentation OCR basée sur Tesseract (Tess4J). La bibliothèque native n'est chargée qu'à
 * l'appel. Tout échec (natif absent, langue manquante, image illisible) est traduit en {@link
 * OcrIndisponibleException} afin que l'utilisateur reçoive un message clair et bascule sur la
 * saisie manuelle.
 */
@Component
public class TesseractOcrEngine implements OcrEngine {

  private final String tessdataPath;
  private final String langue;

  public TesseractOcrEngine(
      @Value("${ocr.tessdata-path:}") String tessdataPath, @Value("${ocr.langue:fra}") String langue) {
    this.tessdataPath = tessdataPath;
    this.langue = langue;
  }

  @Override
  public String extraireTexte(byte[] contenu, String nomFichier) {
    BufferedImage image;
    try {
      image = ImageIO.read(new ByteArrayInputStream(contenu));
    } catch (Exception e) {
      throw new OcrIndisponibleException("Fichier illisible : " + nomFichier, e);
    }
    if (image == null) {
      throw new OcrIndisponibleException(
          "Format non pris en charge (essaie une photo JPG ou PNG nette) : " + nomFichier, null);
    }
    try {
      Tesseract tesseract = new Tesseract();
      if (tessdataPath != null && !tessdataPath.isBlank()) {
        tesseract.setDatapath(tessdataPath);
      }
      tesseract.setLanguage(langue);
      return tesseract.doOCR(image);
    } catch (Exception | LinkageError e) {
      throw new OcrIndisponibleException(
          "Lecture automatique indisponible : saisis tes notes à la main.", e);
    }
  }
}
