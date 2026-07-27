package bj.orientation.model;

/** Une note normalisée : le libellé du relevé a été résolu vers un code canonique. */
public record MatiereNote(String canonique, double note, double coefficient) {
}
