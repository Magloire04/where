package bj.orientation.model;

/**
 * Une matière à pré-afficher dans le formulaire pour une série donnée. {@code coefficient} peut être
 * null quand la valeur officielle n'est pas connue (l'élève la saisit depuis son relevé).
 */
public record MatiereSerie(String code, String libelle, Integer coefficient) {}
