package bj.orientation.calc;

import static org.assertj.core.api.Assertions.assertThat;

import bj.orientation.data.SubjectDictionary;
import org.junit.jupiter.api.Test;

class MatiereResolverTest {
    private final MatiereResolver resolver = new MatiereResolver(new SubjectDictionary());

    @Test
    void casSimpleTroisMatieres() {
        assertThat(resolver.resoudre("Maths / PCT / SVT", "D"))
            .containsExactly("MATHS", "PCT", "SVT");
    }

    @Test
    void casConditionnelChoisitLaClauseDeLaSerie() {
        String raw = "Pour C et D: Maths, PCT, SVT | Pour DEAT: toutes les trois (03) matières écrites";
        assertThat(resolver.resoudre(raw, "C")).containsExactly("MATHS", "PCT", "SVT");
    }

    @Test
    void casDeatDonneListeVide() {
        String raw = "Pour C et D: Maths, PCT, SVT | Pour DEAT: toutes les trois (03) matières écrites";
        assertThat(resolver.resoudre(raw, "DEAT")).isEmpty();
    }

    @Test
    void casOuPrendLePremierReconnu() {
        assertThat(resolver.resoudre("Maths ou Etude de Cas (G) / Français / Anglais", "D"))
            .containsExactly("MATHS", "FR", "ANG");
    }

    @Test
    void nouveauFormatParSerieChoisitLaBonneSerie() {
        String raw =
            "EA : Assainissement, Mobilisation des ressources en eau, PCT "
                + "| B : Economie, Hist-Géo, SVT "
                + "| A1, A2 : Hist-Géo, LV1, SVT "
                + "| C, D : Hist-Géo, PCT, SVT";
        assertThat(resolver.resoudre(raw, "D")).containsExactly("HG", "PCT", "SVT");
        assertThat(resolver.resoudre(raw, "B")).containsExactly("ECO", "HG", "SVT");
    }
}
