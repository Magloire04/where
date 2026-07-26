package bj.orientation.model;

/** Une filière recommandée avec sa moyenne calculée, ses probabilités et son argumentaire. */
public record Recommandation(Filiere filiere, double moyenne, Probabilites proba, String argumentaire) {
}
