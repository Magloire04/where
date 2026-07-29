package bj.orientation.data;

import bj.orientation.model.MatiereSerie;
import bj.orientation.model.Serie;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Charge, au démarrage, les matières à pré-afficher par série (guide de saisie du formulaire),
 * depuis {@code /data/matieres_par_serie.json}. Les libellés sont choisis pour être reconnus par
 * {@link SubjectDictionary} (round-trip garanti par test).
 */
@Component
public class MatiereSerieCatalog {
  private final Map<Serie, List<MatiereSerie>> parSerie;

  public MatiereSerieCatalog() {
    this.parSerie = charger();
  }

  /** Matières à pré-afficher pour une série (liste vide si aucune définie). */
  public List<MatiereSerie> matieres(Serie serie) {
    return parSerie.getOrDefault(serie, List.of());
  }

  private Map<Serie, List<MatiereSerie>> charger() {
    ObjectMapper mapper = new ObjectMapper();
    Map<Serie, List<MatiereSerie>> resultat = new EnumMap<>(Serie.class);
    try (InputStream in = getClass().getResourceAsStream("/data/matieres_par_serie.json")) {
      if (in == null) {
        throw new IllegalStateException("Ressource absente: /data/matieres_par_serie.json");
      }
      JsonNode series = mapper.readTree(in).path("series");
      series
          .fieldNames()
          .forEachRemaining(
              code -> {
                List<MatiereSerie> liste = new ArrayList<>();
                for (JsonNode m : series.path(code)) {
                  JsonNode coef = m.path("coefficient");
                  Integer coefficient = coef.isNumber() ? coef.asInt() : null;
                  liste.add(
                      new MatiereSerie(
                          m.path("code").asText(), m.path("libelle").asText(), coefficient));
                }
                resultat.put(Serie.valueOf(code), List.copyOf(liste));
              });
    } catch (Exception e) {
      throw new IllegalStateException("Échec chargement matieres_par_serie.json", e);
    }
    return resultat;
  }
}
