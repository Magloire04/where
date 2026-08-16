package bj.orientation.web;

import bj.orientation.data.MatiereSerieCatalog;
import bj.orientation.model.MatiereSerie;
import bj.orientation.model.Serie;
import bj.orientation.web.dto.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Métadonnées de référence (séries de bac, matières par série). */
@RestController
@RequestMapping("/api/v1")
public class MetaController {
  private final MatiereSerieCatalog catalogue;

  public MetaController(MatiereSerieCatalog catalogue) {
    this.catalogue = catalogue;
  }

  @GetMapping("/series")
  public ApiResponse<List<String>> series() {
    return new ApiResponse<>(Serie.generales().stream().map(Enum::name).toList());
  }

  @GetMapping("/series/{serie}/matieres")
  public ApiResponse<List<MatiereSerie>> matieres(@PathVariable String serie) {
    return new ApiResponse<>(catalogue.matieres(Serie.fromCode(serie)));
  }
}
