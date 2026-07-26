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
}
