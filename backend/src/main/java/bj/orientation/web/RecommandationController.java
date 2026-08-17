package bj.orientation.web;

import bj.orientation.calc.Recommender;
import bj.orientation.metrics.MetriquesService;
import bj.orientation.model.RecommandationRequest;
import bj.orientation.model.RecommandationResponse;
import bj.orientation.web.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Calcule les recommandations d'orientation à partir d'un profil (série + notes). */
@RestController
@RequestMapping("/api/v1")
public class RecommandationController {
  private final Recommender recommender;
  private final MetriquesService metriques;

  public RecommandationController(Recommender recommender, MetriquesService metriques) {
    this.recommender = recommender;
    this.metriques = metriques;
  }

  @PostMapping("/recommandations")
  public ApiResponse<RecommandationResponse> recommander(
      @Valid @RequestBody RecommandationRequest requete) {
    long debut = System.currentTimeMillis();
    boolean erreur = false;
    try {
      return new ApiResponse<>(recommender.recommander(requete));
    } catch (RuntimeException e) {
      erreur = true;
      throw e;
    } finally {
      metriques.enregistrerCalcul(requete.serie(), System.currentTimeMillis() - debut, erreur);
    }
  }
}
