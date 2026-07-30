package bj.orientation.data;

import static org.assertj.core.api.Assertions.assertThat;

import bj.orientation.model.ModeEntree;
import org.junit.jupiter.api.Test;

class FiliereRepositoryTest {
    private final FiliereRepository repo = new FiliereRepository();

    @Test
    void chargeLes224FilieresPubliques() {
        assertThat(repo.toutes()).hasSize(224);
    }

    @Test
    void medecineGeneraleEstPresenteAvec95Bourses() {
        var medecine = repo.toutes().stream()
            .filter(f -> f.filiere().equals("Médecine Générale"))
            .findFirst().orElseThrow();
        assertThat(medecine.quotaBourse()).isEqualTo(95);
        assertThat(medecine.modeEntree()).isEqualTo(ModeEntree.CLASSEMENT);
    }
}
