package bj.orientation.model;

/** Une ligne extraite d'un relevé par OCR : note et coefficient peuvent être null si non lus. */
public record LigneReleve(String libelle, Double note, Double coefficient) {
}
