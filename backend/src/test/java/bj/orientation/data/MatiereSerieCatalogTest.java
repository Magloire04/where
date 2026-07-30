package bj.orientation.data;

import static org.assertj.core.api.Assertions.assertThat;

import bj.orientation.calc.MatiereResolver;
import bj.orientation.model.Filiere;
import bj.orientation.model.MatiereSerie;
import bj.orientation.model.ModeEntree;
import bj.orientation.model.Serie;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class MatiereSerieCatalogTest {
  private final SubjectDictionary dico = new SubjectDictionary();
  private final MatiereSerieCatalog catalogue = new MatiereSerieCatalog();
  private final FiliereRepository repo = new FiliereRepository();
  private final SerieMatcher serieMatcher = new SerieMatcher();
  private final MatiereResolver resolver = new MatiereResolver(dico);

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

  /**
   * Garantie de couverture : pour chaque série, toute matière dont le calcul a besoin (union des
   * matières résolues sur les filières de classement acceptant la série) est présente au catalogue.
   * Sinon l'élève ne pourrait pas saisir une matière requise et la filière deviendrait incalculable.
   */
  @Test
  void leCatalogueCouvreLesMatieresRequisesParSerie() {
    for (Serie serie : Serie.values()) {
      String code = serie.name();
      Set<String> requises = new HashSet<>();
      for (Filiere f : repo.toutes()) {
        if (f.modeEntree() == ModeEntree.CLASSEMENT
            && serieMatcher.accepte(f.seriesBacRaw(), code)) {
          requises.addAll(resolver.resoudre(f.matieresRaw(), code));
        }
      }
      Set<String> disponibles =
          catalogue.matieres(serie).stream().map(MatiereSerie::code).collect(Collectors.toSet());
      assertThat(disponibles)
          .as("série %s : matières requises non pré-affichées", serie)
          .containsAll(requises);
    }
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
