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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;

/** Orchestre la recommandation : éligibilité, calcul, estimation, classement hybride. */
@Component
public class Recommender {
    private static final double POIDS_AIDE = 0.5;

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
                    List<String> matieres = resolver.resoudre(filiere.matieresRaw(), req.serie());
                    OptionalDouble moyenne = calculator.calculer(matieres, notes);
                    if (moyenne.isEmpty()) {
                        insuffisantes.add(filiere);
                    } else {
                        Probabilites proba = estimator.estimer(filiere, moyenne.getAsDouble());
                        String arg = argumentaire.construire(filiere, moyenne.getAsDouble(), proba);
                        scorables.add(new Recommandation(filiere, moyenne.getAsDouble(), proba, arg));
                    }
                }
            }
        }

        scorables.sort(Comparator.comparingDouble(this::score).reversed());
        List<Recommandation> top3 = new ArrayList<>(scorables.subList(0, Math.min(3, scorables.size())));
        List<Recommandation> alternatives = new ArrayList<>(
            scorables.subList(Math.min(3, scorables.size()), Math.min(8, scorables.size())));

        return new RecommandationResponse(top3, alternatives, concours, payantes, insuffisantes);
    }

    private double score(Recommandation reco) {
        return reco.proba().pBourse() + POIDS_AIDE * reco.proba().pAide();
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
