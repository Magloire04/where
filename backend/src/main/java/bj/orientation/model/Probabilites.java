package bj.orientation.model;

/** Résultat de l'estimateur pour une filière : les 3 probabilités + le statut le plus probable. */
public record Probabilites(
        double pBourse,
        double pAide,
        double pPayant,
        StatutEstime statut,
        double pctAffiche) {
}
