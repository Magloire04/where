package bj.orientation.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Requête de recommandation : série du candidat, notes saisies, domaine optionnel (V2). */
public record RecommandationRequest(
        @NotBlank String serie,
        @NotEmpty @Valid List<NoteSaisie> notes,
        String domaine) {
}
