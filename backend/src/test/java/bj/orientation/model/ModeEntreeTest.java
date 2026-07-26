package bj.orientation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModeEntreeTest {
    @Test
    void parseMappeLesLibellesDuGuide() {
        assertThat(ModeEntree.parse("Classement")).isEqualTo(ModeEntree.CLASSEMENT);
        assertThat(ModeEntree.parse("Concours")).isEqualTo(ModeEntree.CONCOURS);
        assertThat(ModeEntree.parse("A titre payant")).isEqualTo(ModeEntree.PAYANT);
    }

    @Test
    void parseToleranteAuNullEtAccents() {
        assertThat(ModeEntree.parse(null)).isEqualTo(ModeEntree.CLASSEMENT);
        assertThat(ModeEntree.parse("à titre payant")).isEqualTo(ModeEntree.PAYANT);
    }
}
