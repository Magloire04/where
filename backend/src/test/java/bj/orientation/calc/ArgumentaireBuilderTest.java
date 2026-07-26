package bj.orientation.calc;

import bj.orientation.model.Filiere;
import bj.orientation.model.ModeEntree;
import bj.orientation.model.Probabilites;
import bj.orientation.model.StatutEstime;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArgumentaireBuilderTest {
    private final ArgumentaireBuilder builder = new ArgumentaireBuilder();

    private Filiere filiere(int bourse, int aide) {
        return new Filiere(1, "UAC", "FSS", "Médecine Générale", bourse, aide,
            ModeEntree.CLASSEMENT, "C, D", "Maths / PCT / SVT", List.of("Médecin généraliste"), 31);
    }

    @Test
    void argumentaireBoursierContientLaFiliereEtLeDisclaimer() {
        var proba = new Probabilites(0.8, 0.0, 0.2, StatutEstime.BOURSIER, 0.8);
        String texte = builder.construire(filiere(150, 0), 16.0, proba);
        assertThat(texte).contains("Médecine Générale").contains("FSS");
        assertThat(texte).endsWith(ArgumentaireBuilder.DISCLAIMER);
    }

    @Test
    void argumentaireAideMentionneLesPlacesDAide() {
        var proba = new Probabilites(0.2, 0.6, 0.2, StatutEstime.AIDE, 0.6);
        String texte = builder.construire(filiere(60, 340), 11.5, proba);
        assertThat(texte).contains("340");
    }
}
