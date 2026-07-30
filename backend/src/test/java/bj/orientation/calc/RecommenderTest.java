package bj.orientation.calc;

import static org.assertj.core.api.Assertions.assertThat;

import bj.orientation.config.EstimateurProperties;
import bj.orientation.data.FiliereRepository;
import bj.orientation.data.SerieMatcher;
import bj.orientation.data.SubjectDictionary;
import bj.orientation.model.ModeEntree;
import bj.orientation.model.NoteSaisie;
import bj.orientation.model.Palier;
import bj.orientation.model.RecommandationRequest;
import bj.orientation.model.RecommandationResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RecommenderTest {
    private Recommender build() {
        var props = new EstimateurProperties(
            1.2,
            Map.of(
                Palier.T1, new EstimateurProperties.Seuils(15.0, null),
                Palier.T2, new EstimateurProperties.Seuils(13.0, 11.5),
                Palier.T3, new EstimateurProperties.Seuils(11.5, 10.5),
                Palier.T4, new EstimateurProperties.Seuils(10.5, 10.0)),
            List.of("Médecine Générale"));
        var dico = new SubjectDictionary();
        return new Recommender(new FiliereRepository(), new SerieMatcher(), dico,
            new MatiereResolver(dico), new MoyenneCalculator(),
            new ProbabilityEstimator(new SelectiviteTiering(props), props),
            new ArgumentaireBuilder());
    }

    @Test
    void eleveDFortRecoitTop3NonVide() {
        var req = new RecommandationRequest("D", List.of(
            new NoteSaisie("Maths", 16, 4),
            new NoteSaisie("PCT", 15, 4),
            new NoteSaisie("SVT", 17, 5)), null);
        RecommandationResponse resp = build().recommander(req);
        assertThat(resp.top3()).isNotEmpty().hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void resultatsClassesParChanceAllocation() {
        var req = new RecommandationRequest("D", List.of(
            new NoteSaisie("Maths", 14, 4),
            new NoteSaisie("PCT", 13, 4),
            new NoteSaisie("SVT", 15, 5)), null);
        RecommandationResponse resp = build().recommander(req);
        // Bourse OU aide : le classement suit la probabilité d'obtenir une allocation.
        var chances = java.util.stream.Stream.concat(
                resp.top3().stream(), resp.alternatives().stream())
            .map(r -> r.proba().pBourse() + r.proba().pAide()).toList();
        assertThat(chances).isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Test
    void serieF3ProduitDesEstimations() {
        // F3 accepte des filières de classement résolues en {Français, Maths, PCT}.
        var req = new RecommandationRequest("F3", List.of(
            new NoteSaisie("Maths", 14, 3),
            new NoteSaisie("PCT", 13, 3),
            new NoteSaisie("Français", 12, 2)), null);
        RecommandationResponse resp = build().recommander(req);
        assertThat(resp.top3()).isNotEmpty();
    }

    @Test
    void filieresConcoursSontListeesAPart() {
        var req = new RecommandationRequest("D", List.of(
            new NoteSaisie("PCT", 14, 4),
            new NoteSaisie("SVT", 14, 5),
            new NoteSaisie("Maths", 12, 4)), null);
        RecommandationResponse resp = build().recommander(req);
        assertThat(resp.concours()).anyMatch(f -> f.modeEntree() == ModeEntree.CONCOURS);
    }
}
