package bj.orientation.config;

import static org.assertj.core.api.Assertions.assertThat;

import bj.orientation.model.Palier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EstimateurPropertiesTest {
    @Autowired
    EstimateurProperties props;

    @Test
    void chargeLesSeuilsDepuisApplicationYml() {
        assertThat(props.sigma()).isEqualTo(1.2);
        assertThat(props.paliers().get(Palier.T1).bourse()).isEqualTo(15.0);
        assertThat(props.paliers().get(Palier.T4).aide()).isEqualTo(10.0);
        assertThat(props.prestige()).contains("Médecine Générale");
    }
}
