package bj.orientation.web;

import bj.orientation.calc.Recommender;
import bj.orientation.model.RecommandationRequest;
import bj.orientation.model.RecommandationResponse;
import bj.orientation.web.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Calcule les recommandations d'orientation à partir d'un profil (série + notes). */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class RecommandationController {
    private final Recommender recommender;

    public RecommandationController(Recommender recommender) {
        this.recommender = recommender;
    }

    @PostMapping("/recommandations")
    public ApiResponse<RecommandationResponse> recommander(@Valid @RequestBody RecommandationRequest requete) {
        return new ApiResponse<>(recommender.recommander(requete));
    }
}
