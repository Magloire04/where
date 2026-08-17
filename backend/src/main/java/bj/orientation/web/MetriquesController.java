package bj.orientation.web;

import bj.orientation.metrics.MetriquesService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Enregistre une visite (chargement de l'application) pour les statistiques d'usage. */
@RestController
@RequestMapping("/api/v1/metriques")
public class MetriquesController {
  private final MetriquesService metriques;

  public MetriquesController(MetriquesService metriques) {
    this.metriques = metriques;
  }

  @PostMapping("/visite")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void visite() {
    metriques.enregistrerVisite();
  }
}
