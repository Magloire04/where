package bj.orientation.calc;

import bj.orientation.data.FiliereRepository;
import bj.orientation.data.SerieMatcher;
import bj.orientation.data.SubjectDictionary;
import bj.orientation.model.Filiere;
import bj.orientation.model.MatiereNote;
import bj.orientation.model.ModeEntree;
import bj.orientation.model.NoteSaisie;
import bj.orientation.model.Probabilites;
import bj.orientation.model.Recommandation;
import bj.orientation.model.RecommandationRequest;
import bj.orientation.model.RecommandationResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Orchestre la recommandation : éligibilité, calcul sur triplet complet, estimation, classement. */
@Component
public class Recommender {
    // Classement par chance d'obtenir une allocation (bourse OU aide), la bourse départageant.
    private static final Comparator<Recommandation> PAR_CHANCE_ALLOCATION =
        Comparator.comparingDouble((Recommandation r) -> r.proba().pBourse() + r.proba().pAide())
            .thenComparingDouble(r -> r.proba().pBourse())
            .reversed();

    // Une filière n'est retenue que si son triplet partage au moins autant de matières avec les
    // matières fortes déclarées par l'élève.
    private static final int MIN_CORRESPONDANCE = 2;

    private final FiliereRepository repo;
    private final SerieMatcher serieMatcher;
    private final SubjectDictionary dico;
    private final MatiereResolver resolver;
    private final MoyenneCalculator calculator;
    private final ProbabilityEstimator estimator;
    private final ArgumentaireBuilder argumentaire;

    public Recommender(FiliereRepository repo, SerieMatcher serieMatcher, SubjectDictionary dico,
                       MatiereResolver resolver, MoyenneCalculator calculator,
                       ProbabilityEstimator estimator, ArgumentaireBuilder argumentaire) {
        this.repo = repo;
        this.serieMatcher = serieMatcher;
        this.dico = dico;
        this.resolver = resolver;
        this.calculator = calculator;
        this.estimator = estimator;
        this.argumentaire = argumentaire;
    }

    public RecommandationResponse recommander(RecommandationRequest req) {
        List<MatiereNote> notes = normaliser(req.notes());
        Set<String> codesSaisis = notes.stream().map(MatiereNote::canonique).collect(Collectors.toSet());
        Set<String> codesForts = req.matieresFortes().stream()
            .map(dico::canonique)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        List<Recommandation> recommandations = new ArrayList<>();
        List<Filiere> concours = new ArrayList<>();
        Set<String> aCompleter = new LinkedHashSet<>();

        for (Filiere filiere : repo.toutes()) {
            if (!serieMatcher.accepte(filiere.seriesBacRaw(), req.serie())) {
                continue;
            }
            if (filiere.modeEntree() == ModeEntree.CONCOURS) {
                concours.add(filiere);
                continue;
            }
            if (filiere.modeEntree() != ModeEntree.CLASSEMENT) {
                continue;
            }
            List<String> triplet = resolver.resoudre(filiere.matieresRaw(), req.serie());
            if (triplet.isEmpty()) {
                continue;
            }
            long communesFortes = triplet.stream().filter(codesForts::contains).count();
            if (communesFortes < MIN_CORRESPONDANCE) {
                // Pas assez pertinent au regard des matières fortes de l'élève.
                continue;
            }
            List<String> manquantes = triplet.stream().filter(m -> !codesSaisis.contains(m)).toList();
            if (!manquantes.isEmpty()) {
                // Filière pertinente mais dont le calcul exige une matière non encore saisie.
                aCompleter.addAll(manquantes);
                continue;
            }
            OptionalDouble moyenne = calculator.calculer(triplet, notes);
            if (moyenne.isEmpty()) {
                continue;
            }
            Probabilites proba = estimator.estimer(filiere, moyenne.getAsDouble());
            String arg = argumentaire.construire(filiere, moyenne.getAsDouble(), proba);
            recommandations.add(new Recommandation(filiere, moyenne.getAsDouble(), proba, arg, triplet));
        }

        recommandations.sort(PAR_CHANCE_ALLOCATION);
        return new RecommandationResponse(recommandations, new ArrayList<>(aCompleter), concours);
    }

    private List<MatiereNote> normaliser(List<NoteSaisie> saisies) {
        List<MatiereNote> resultat = new ArrayList<>();
        for (NoteSaisie saisie : saisies) {
            String canon = dico.canonique(saisie.libelle());
            if (canon != null) {
                resultat.add(new MatiereNote(canon, saisie.note(), saisie.coefficient()));
            }
        }
        return resultat;
    }
}
