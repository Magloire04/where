package bj.orientation.calc;

import bj.orientation.config.EstimateurProperties;
import bj.orientation.model.Filiere;
import bj.orientation.model.ModeEntree;
import bj.orientation.model.Palier;
import bj.orientation.model.Probabilites;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProbabilityEstimatorTest {
    private final EstimateurProperties props = new EstimateurProperties(
        1.2,
        Map.of(
            Palier.T1, new EstimateurProperties.Seuils(15.0, null),
            Palier.T2, new EstimateurProperties.Seuils(13.0, 11.5),
            Palier.T3, new EstimateurProperties.Seuils(11.5, 10.5),
            Palier.T4, new EstimateurProperties.Seuils(10.5, 10.0)),
        List.of("Médecine Générale"));
    private final ProbabilityEstimator estimator =
        new ProbabilityEstimator(new SelectiviteTiering(props), props);

    private Filiere filiere(String nom, int bourse, int aide) {
        return new Filiere(1, "U", "E", nom, bourse, aide, ModeEntree.CLASSEMENT,
            "C, D", "Maths / PCT / SVT", List.of(), 1);
    }

    @Test
    void sommeDesProbasVautUn() {
        Probabilites p = estimator.estimer(filiere("Psychologie", 44, 60), 12.0);
        assertThat(p.pBourse() + p.pAide() + p.pPayant()).isCloseTo(1.0, Offset.offset(1e-9));
    }

    @Test
    void pBourseCroitAvecLaMoyenne() {
        Filiere medecine = filiere("Médecine Générale", 150, 0);
        assertThat(estimator.estimer(medecine, 17).pBourse())
            .isGreaterThan(estimator.estimer(medecine, 12).pBourse());
    }

    @Test
    void pAideNulleQuandAucunCoussin() {
        assertThat(estimator.estimer(filiere("Médecine Générale", 150, 0), 14).pAide()).isEqualTo(0.0);
    }

    @Test
    void pBourseEstBornee() {
        assertThat(estimator.estimer(filiere("Médecine Générale", 150, 0), 20).pBourse()).isLessThanOrEqualTo(0.98);
        assertThat(estimator.estimer(filiere("Médecine Générale", 150, 0), 2).pBourse()).isGreaterThanOrEqualTo(0.02);
    }
}
