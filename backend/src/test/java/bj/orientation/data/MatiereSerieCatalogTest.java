package bj.orientation.data;

import static org.assertj.core.api.Assertions.assertThat;

import bj.orientation.model.MatiereSerie;
import bj.orientation.model.Serie;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class MatiereSerieCatalogTest {
  private final SubjectDictionary dico = new SubjectDictionary();
  private final MatiereSerieCatalog catalogue = new MatiereSerieCatalog();

  /**
   * Chaque libellé pré-affiché DOIT être reconnu par le dictionnaire (sinon le calcul de moyenne ne
   * retrouve pas la matière saisie par l'élève). Garantie forte, pour toutes les séries.
   */
  @Test
  void chaqueLibelleEstReconnuParLeDictionnaire() {
    for (Serie serie : Serie.values()) {
      for (MatiereSerie m : catalogue.matieres(serie)) {
        assertThat(dico.canonique(m.libelle()))
            .as("libellé « %s » (série %s) doit résoudre vers %s", m.libelle(), serie, m.code())
            .isEqualTo(m.code());
      }
    }
  }

  @Test
  void seriesCEtDExposentLesMatieresScientifiquesCles() {
    for (Serie serie : List.of(Serie.C, Serie.D)) {
      var codes =
          catalogue.matieres(serie).stream().map(MatiereSerie::code).collect(Collectors.toSet());
      assertThat(codes)
          .as("série %s doit exposer Maths/PCT/SVT/Français (filières scientifiques)", serie)
          .contains("MATHS", "PCT", "SVT", "FR");
    }
  }

  @Test
  void coefficientsScientifiquesConfirmesPourCEtD() {
    assertThat(coef(Serie.D, "SVT")).isEqualTo(5);
    assertThat(coef(Serie.D, "MATHS")).isEqualTo(4);
    assertThat(coef(Serie.D, "PCT")).isEqualTo(4);
    assertThat(coef(Serie.C, "MATHS")).isEqualTo(6);
    assertThat(coef(Serie.C, "PCT")).isEqualTo(5);
    assertThat(coef(Serie.C, "SVT")).isEqualTo(2);
  }

  @Test
  void toutesLesSeriesOntAuMoinsUneMatiere() {
    for (Serie serie : Serie.values()) {
      assertThat(catalogue.matieres(serie))
          .as("série %s doit avoir au moins une matière pré-affichée", serie)
          .isNotEmpty();
    }
  }

  private Integer coef(Serie serie, String code) {
    return catalogue.matieres(serie).stream()
        .filter(m -> m.code().equals(code))
        .findFirst()
        .orElseThrow()
        .coefficient();
  }
}
