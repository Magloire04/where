package bj.orientation.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Requête de recommandation : série du candidat, notes saisies (matières fortes + éventuelles
 * matières complétées), libellés des 3 matières fortes déclarées (filtre de pertinence).
 */
public record RecommandationRequest(
        @NotBlank String serie,
        @NotEmpty @Valid List<NoteSaisie> notes,
        @NotEmpty List<String> matieresFortes,
        String domaine) {
}
