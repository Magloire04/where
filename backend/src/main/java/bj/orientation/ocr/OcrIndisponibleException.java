package bj.orientation.ocr;

/** Levée quand l'OCR ne peut aboutir (natif absent, fichier illisible) → repli saisie manuelle. */
public class OcrIndisponibleException extends RuntimeException {
    public OcrIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
