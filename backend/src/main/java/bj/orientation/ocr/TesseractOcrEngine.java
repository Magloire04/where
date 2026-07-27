package bj.orientation.ocr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implémentation OCR basée sur Tesseract (Tess4J). La bibliothèque native n'est chargée
 * qu'à l'appel {@link #extraireTexte} (jamais au démarrage), pour que le contexte se charge
 * même sans binaire natif. Tout échec est traduit en {@link OcrIndisponibleException}.
 */
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
        } catch (TesseractException
                | java.io.IOException
                | UnsatisfiedLinkError
                | NoClassDefFoundError e) {
            throw new OcrIndisponibleException(
                "OCR indisponible sur ce serveur : saisissez vos notes manuellement.", e);
        }
    }
}
