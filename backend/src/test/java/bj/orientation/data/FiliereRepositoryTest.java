package bj.orientation.data;

import static org.assertj.core.api.Assertions.assertThat;

import bj.orientation.model.ModeEntree;
import org.junit.jupiter.api.Test;

class FiliereRepositoryTest {
    private final FiliereRepository repo = new FiliereRepository();

    @Test
    void chargeLes216FilieresPubliques() {
        assertThat(repo.toutes()).hasSize(216);
    }

    @Test
    void medecineGeneraleEstPresenteAvec150Bourses() {
        var medecine = repo.toutes().stream()
            .filter(f -> f.filiere().equals("Médecine Générale"))
            .findFirst().orElseThrow();
        assertThat(medecine.quotaBourse()).isEqualTo(150);
        assertThat(medecine.modeEntree()).isEqualTo(ModeEntree.CLASSEMENT);
    }
}
