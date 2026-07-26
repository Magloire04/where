package bj.orientation.calc;

import static org.assertj.core.api.Assertions.assertThat;

import bj.orientation.model.MatiereNote;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

class MoyenneCalculatorTest {
    private final MoyenneCalculator calculator = new MoyenneCalculator();

    @Test
    void goldenMedecineBacD_svt5_maths4_spct4() {
        var notes = List.of(
            new MatiereNote("SVT", 15, 5),
            new MatiereNote("MATHS", 10, 4),
            new MatiereNote("PCT", 13, 4));
        double attendu = (15 * 5 + 10 * 4 + 13 * 4) / 13.0;
        assertThat(calculator.calculer(List.of("MATHS", "PCT", "SVT"), notes).getAsDouble())
            .isCloseTo(attendu, Offset.offset(1e-9));
    }

    @Test
    void matiereManquanteRendVide() {
        var notes = List.of(new MatiereNote("MATHS", 12, 4));
        assertThat(calculator.calculer(List.of("MATHS", "PCT", "SVT"), notes)).isEmpty();
    }
}
