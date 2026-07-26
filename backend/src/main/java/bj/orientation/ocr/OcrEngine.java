package bj.orientation.ocr;

/** Abstraction du moteur d'OCR (isole la dépendance native du reste de l'application). */
public interface OcrEngine {
    String extraireTexte(byte[] contenu, String nomFichier);
}
