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

    private RecommandationRequest req(String serie, List<String> fortes, NoteSaisie... notes) {
        return new RecommandationRequest(serie, List.of(notes), fortes, null);
    }

    @Test
    void filiereCalculeeSurSonTripletComplet() {
        var r = build().recommander(req("D", List.of("Maths", "PCT", "SVT"),
            new NoteSaisie("Maths", 15, 4),
            new NoteSaisie("PCT", 14, 4),
            new NoteSaisie("SVT", 16, 5)));
        assertThat(r.recommandations()).isNotEmpty();
        assertThat(r.recommandations())
            .allSatisfy(reco -> assertThat(reco.matieresRetenues()).hasSize(3));
    }

    @Test
    void filiere2sur3NonComplete_estEnAttenteEtMatiereManquanteListee() {
        // Fortes = Anglais, Hist-Géo, Philo. Commerce International (série D) = Anglais, Hist-Géo,
        // Maths : 2 fortes communes mais Maths manque -> à compléter, pas calculée.
        var r = build().recommander(req("D", List.of("Anglais", "Hist-Géo", "Philosophie"),
            new NoteSaisie("Anglais", 15, 2),
            new NoteSaisie("Hist-Géo", 14, 2),
            new NoteSaisie("Philosophie", 13, 2)));
        assertThat(r.matieresACompleter()).contains("MATHS");
        assertThat(r.recommandations())
            .allSatisfy(reco -> assertThat(reco.matieresRetenues()).hasSize(3));
        var noms = r.recommandations().stream().map(reco -> reco.filiere().filiere()).toList();
        assertThat(noms).doesNotContain("Commerce International");
    }

    @Test
    void apresCompletionDeLaMatiereManquante_laFiliereEstScoree() {
        var r = build().recommander(req("D", List.of("Anglais", "Hist-Géo", "Philosophie"),
            new NoteSaisie("Anglais", 15, 2),
            new NoteSaisie("Hist-Géo", 14, 2),
            new NoteSaisie("Philosophie", 13, 2),
            new NoteSaisie("Maths", 12, 4)));
        var noms = r.recommandations().stream().map(reco -> reco.filiere().filiere()).toList();
        assertThat(noms).contains("Commerce International");
        assertThat(r.recommandations())
            .allSatisfy(reco -> assertThat(reco.matieresRetenues()).hasSize(3));
    }

    @Test
    void filtrePertinence_moinsDeDeuxFortesCommunes_exclut() {
        // Fortes littéraires : les filières scientifiques (Maths/PCT/SVT) ne partagent aucune forte.
        var r = build().recommander(req("D", List.of("Français", "Hist-Géo", "Philosophie"),
            new NoteSaisie("Français", 16, 2),
            new NoteSaisie("Hist-Géo", 15, 2),
            new NoteSaisie("Philosophie", 14, 2)));
        assertThat(r.recommandations()).isNotEmpty();
        var noms = r.recommandations().stream().map(reco -> reco.filiere().filiere()).toList();
        assertThat(noms).doesNotContain("Médecine Générale");
    }

    @Test
    void resultatsClassesParChanceAllocation() {
        var r = build().recommander(req("D", List.of("Maths", "PCT", "SVT"),
            new NoteSaisie("Maths", 14, 4),
            new NoteSaisie("PCT", 13, 4),
            new NoteSaisie("SVT", 15, 5)));
        var chances = r.recommandations().stream()
            .map(reco -> reco.proba().pBourse() + reco.proba().pAide()).toList();
        assertThat(chances).isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Test
    void filieresConcoursSontListeesAPart() {
        var r = build().recommander(req("D", List.of("Maths", "PCT", "SVT"),
            new NoteSaisie("PCT", 14, 4),
            new NoteSaisie("SVT", 14, 5),
            new NoteSaisie("Maths", 12, 4)));
        assertThat(r.concours()).anyMatch(f -> f.modeEntree() == ModeEntree.CONCOURS);
    }
}
