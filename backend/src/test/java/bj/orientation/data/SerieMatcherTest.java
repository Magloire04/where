package bj.orientation.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SerieMatcherTest {
    private final SerieMatcher matcher = new SerieMatcher();

    @Test
    void matchLeCodeSerieCommeTokenExact() {
        assertThat(matcher.accepte("A1, A2, B, C, D, DEAT (toutes spécialités) et DT/STI", "C")).isTrue();
        assertThat(matcher.accepte("A1, A2, B, C, D", "D")).isTrue();
        assertThat(matcher.accepte("C, D", "A1")).isFalse();
    }

    @Test
    void neMatchePasUnCodeContenuDansUnAutreToken() {
        assertThat(matcher.accepte("DEAT (toutes options)", "D")).isFalse();
    }

    @Test
    void matchDtEtDeatCommeFamilles() {
        assertThat(matcher.accepte("C, D, DT/IMI, DT/DWM", "DT")).isTrue();
        assertThat(matcher.accepte("C, D et DEAT/(toutes options)", "DEAT")).isTrue();
    }
}
