package bj.orientation.model;

import java.util.List;

/**
 * Une filière recommandée avec sa moyenne calculée, ses probabilités, son argumentaire et les
 * matières (parmi celles déclarées par l'élève) réellement retenues pour le calcul.
 */
public record Recommandation(
        Filiere filiere,
        double moyenne,
        Probabilites proba,
        String argumentaire,
        List<String> matieresRetenues) {
}
