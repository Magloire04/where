package bj.orientation.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SubjectDictionaryTest {
    private final SubjectDictionary dico = new SubjectDictionary();

    @Test
    void normaliseLesVariantesVersLeCanonique() {
        assertThat(dico.canonique("Maths")).isEqualTo("MATHS");
        assertThat(dico.canonique("Mathématiques")).isEqualTo("MATHS");
        assertThat(dico.canonique("PCT")).isEqualTo("PCT");
        assertThat(dico.canonique("SPCT")).isEqualTo("PCT");
        assertThat(dico.canonique("Sciences Physiques")).isEqualTo("PCT");
        assertThat(dico.canonique("SVT")).isEqualTo("SVT");
        assertThat(dico.canonique("Français")).isEqualTo("FR");
        assertThat(dico.canonique("Hist-Géo")).isEqualTo("HG");
        assertThat(dico.canonique("Anglais (LV1)")).isEqualTo("ANG");
        assertThat(dico.canonique("Philo")).isEqualTo("PHILO");
        assertThat(dico.canonique("matière inconnue xyz")).isNull();
    }
}
