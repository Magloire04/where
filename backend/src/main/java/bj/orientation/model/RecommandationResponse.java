package bj.orientation.model;

import java.util.List;

/**
 * Réponse de recommandation :
 * <ul>
 *   <li>{@code recommandations} : filières calculées sur leur triplet complet, triées par chance ;
 *   <li>{@code matieresACompleter} : codes des matières manquantes à saisir pour couvrir des
 *       filières qui ne partagent que 2 des 3 matières fortes ;
 *   <li>{@code concours} : filières accessibles mais recrutant par concours (non calculées ainsi).
 * </ul>
 */
public record RecommandationResponse(
        List<Recommandation> recommandations,
        List<String> matieresACompleter,
        List<Filiere> concours) {
}
