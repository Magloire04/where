package bj.orientation.ocr;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReleveParserTest {
    private final ReleveParser parser = new ReleveParser();

    @Test
    void litLibelleNoteEtCoefficient() {
        var lignes = parser.parser("Mathématiques 12,50 4");
        assertThat(lignes).hasSize(1);
        assertThat(lignes.get(0).libelle()).isEqualTo("Mathématiques");
        assertThat(lignes.get(0).note()).isEqualTo(12.5);
        assertThat(lignes.get(0).coefficient()).isEqualTo(4.0);
    }

    @Test
    void ignoreLesLignesSansNote() {
        assertThat(parser.parser("RELEVE DE NOTES\nEtablissement: Lycee")).isEmpty();
    }

    @Test
    void gereLePointDecimalEtPlusieursLignes() {
        var lignes = parser.parser("Physique-Chimie 09.00 4\nAnglais 14 2");
        assertThat(lignes).hasSize(2);
        assertThat(lignes.get(0).note()).isEqualTo(9.0);
        assertThat(lignes.get(1).coefficient()).isEqualTo(2.0);
    }
}
