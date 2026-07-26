package bj.orientation.model;

import java.util.List;

/** Réponse de recommandation : top-3, alternatives, et filières listées à part. */
public record RecommandationResponse(
        List<Recommandation> top3,
        List<Recommandation> alternatives,
        List<Filiere> concours,
        List<Filiere> payantes,
        List<Filiere> donneesInsuffisantes) {
}
