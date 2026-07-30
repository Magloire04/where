package bj.orientation.calc;

import bj.orientation.config.EstimateurProperties;
import bj.orientation.model.Filiere;
import bj.orientation.model.Palier;
import bj.orientation.model.Probabilites;
import bj.orientation.model.StatutEstime;
import org.springframework.stereotype.Component;

/** Transforme une moyenne de classement en probabilités de statut (logistique par palier). */
@Component
public class ProbabilityEstimator {
    private static final double BORNE_MIN = 0.02;
    private static final double BORNE_MAX = 0.98;

    private final SelectiviteTiering tiering;
    private final EstimateurProperties props;

    public ProbabilityEstimator(SelectiviteTiering tiering, EstimateurProperties props) {
        this.tiering = tiering;
        this.props = props;
    }

    public Probabilites estimer(Filiere filiere, double moyenne) {
        Palier palier = tiering.palier(filiere);
        EstimateurProperties.Seuils seuils = props.paliers().get(palier);
        double sigma = props.sigma();

        // Pas de bourse octroyée dans cette filière => impossible d'être boursier.
        double pBourse = 0.0;
        if (filiere.quotaBourse() > 0) {
            pBourse = clamp(logistique((moyenne - seuils.bourse()) / sigma), BORNE_MIN, BORNE_MAX);
        }

        double pAide = 0.0;
        if (filiere.quotaAideFpp() > 0) {
            double seuilAide = seuils.aide() != null ? seuils.aide() : seuils.bourse() - 1.0;
            pAide = (1 - pBourse) * logistique((moyenne - seuilAide) / sigma);
        }
        double pPayant = Math.max(0.0, 1 - pBourse - pAide);

        StatutEstime statut;
        double pct;
        if (pBourse >= pAide && pBourse >= pPayant) {
            statut = StatutEstime.BOURSIER;
            pct = pBourse;
        } else if (pAide >= pPayant) {
            statut = StatutEstime.AIDE;
            pct = pAide;
        } else {
            statut = StatutEstime.PAYANT;
            pct = pPayant;
        }
        return new Probabilites(pBourse, pAide, pPayant, statut, pct);
    }

    private static double logistique(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private static double clamp(double valeur, double min, double max) {
        return Math.max(min, Math.min(max, valeur));
    }
}
