package bj.orientation.calc;

import bj.orientation.config.EstimateurProperties;
import bj.orientation.model.Filiere;
import bj.orientation.model.ModeEntree;
import bj.orientation.model.Palier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SelectiviteTieringTest {
    private final EstimateurProperties props = new EstimateurProperties(
        1.2,
        Map.of(
            Palier.T1, new EstimateurProperties.Seuils(15.0, null),
            Palier.T2, new EstimateurProperties.Seuils(13.0, 11.5),
            Palier.T3, new EstimateurProperties.Seuils(11.5, 10.5),
            Palier.T4, new EstimateurProperties.Seuils(10.5, 10.0)),
        List.of("Médecine Générale"));
    private final SelectiviteTiering tiering = new SelectiviteTiering(props);

    private Filiere filiere(String nom, int bourse, int aide) {
        return new Filiere(1, "U", "E", nom, bourse, aide, ModeEntree.CLASSEMENT,
            "C, D", "Maths / PCT / SVT", List.of(), 1);
    }

    @Test
    void prestigeSansAideEstT1() {
        assertThat(tiering.palier(filiere("Médecine Générale", 150, 0))).isEqualTo(Palier.T1);
    }

    @Test
    void grosCoussinAideEstT4() {
        assertThat(tiering.palier(filiere("Sciences Economiques", 207, 1407))).isEqualTo(Palier.T4);
    }

    @Test
    void aideModereeEstT3() {
        assertThat(tiering.palier(filiere("Psychologie", 44, 60))).isEqualTo(Palier.T3);
    }

    @Test
    void sansAideNonPrestigeEstT3() {
        assertThat(tiering.palier(filiere("Andragogie", 8, 0))).isEqualTo(Palier.T3);
    }
}
