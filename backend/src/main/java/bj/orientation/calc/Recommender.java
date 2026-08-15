package bj.orientation.calc;

import bj.orientation.data.FiliereRepository;
import bj.orientation.data.SerieMatcher;
import bj.orientation.data.SubjectDictionary;
import bj.orientation.model.Filiere;
import bj.orientation.model.MatiereNote;
import bj.orientation.model.NoteSaisie;
import bj.orientation.model.Probabilites;
import bj.orientation.model.Recommandation;
import bj.orientation.model.RecommandationRequest;
import bj.orientation.model.RecommandationResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Orchestre la recommandation : éligibilité, calcul, estimation, classement hybride. */
@Component
public class Recommender {
    // Classement par chance d'obtenir une allocation (bourse OU aide), la bourse départageant.
    private static final Comparator<Recommandation> PAR_CHANCE_ALLOCATION =
        Comparator.comparingDouble((Recommandation r) -> r.proba().pBourse() + r.proba().pAide())
            .thenComparingDouble(r -> r.proba().pBourse())
            .reversed();

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

    // Correspondance minimale : une filière n'est retenue que si son triplet de calcul partage
    // au moins ce nombre de matières avec les matières fortes déclarées par l'élève.
    private static final int MIN_CORRESPONDANCE = 2;

    public RecommandationResponse recommander(RecommandationRequest req) {
        List<MatiereNote> notes = normaliser(req.notes());
        Set<String> codesSaisis = notes.stream().map(MatiereNote::canonique).collect(Collectors.toSet());
        List<Recommandation> scorables = new ArrayList<>();
        List<Filiere> concours = new ArrayList<>();
        List<Filiere> payantes = new ArrayList<>();
        List<Filiere> insuffisantes = new ArrayList<>();

        for (Filiere filiere : repo.toutes()) {
            if (!serieMatcher.accepte(filiere.seriesBacRaw(), req.serie())) {
                continue;
            }
            switch (filiere.modeEntree()) {
                case CONCOURS -> concours.add(filiere);
                case PAYANT -> payantes.add(filiere);
                case CLASSEMENT -> {
                    List<String> triplet = resolver.resoudre(filiere.matieresRaw(), req.serie());
                    List<String> retenues =
                        triplet.stream().filter(codesSaisis::contains).toList();
                    if (retenues.size() < MIN_CORRESPONDANCE) {
                        // La filière ne calcule pas assez sur les matières fortes de l'élève.
                        continue;
                    }
                    OptionalDouble moyenne = calculator.calculer(retenues, notes);
                    if (moyenne.isEmpty()) {
                        insuffisantes.add(filiere);
                    } else {
                        Probabilites proba = estimator.estimer(filiere, moyenne.getAsDouble());
                        String arg = argumentaire.construire(filiere, moyenne.getAsDouble(), proba);
                        scorables.add(
                            new Recommandation(filiere, moyenne.getAsDouble(), proba, arg, retenues));
                    }
                }
            }
        }

        scorables.sort(PAR_CHANCE_ALLOCATION);
        List<Recommandation> top3 = new ArrayList<>(scorables.subList(0, Math.min(3, scorables.size())));
        List<Recommandation> alternatives = new ArrayList<>(
            scorables.subList(Math.min(3, scorables.size()), Math.min(8, scorables.size())));

        return new RecommandationResponse(top3, alternatives, concours, payantes, insuffisantes);
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
