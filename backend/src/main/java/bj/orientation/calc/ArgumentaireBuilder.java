package bj.orientation.calc;

import bj.orientation.model.Filiere;
import bj.orientation.model.Probabilites;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Construit l'argumentaire (quelques lignes) défendant une recommandation, sans LLM. */
@Component
public class ArgumentaireBuilder {
    public static final String DISCLAIMER = "Estimation indicative, pas une garantie de sélection.";

    public String construire(Filiere filiere, double moyenne, Probabilites proba) {
        String moyenneTexte = String.format(Locale.FRANCE, "%.2f", moyenne);
        String pct = String.format(Locale.FRANCE, "%.0f%%", proba.pctAffiche() * 100);
        String base = switch (proba.statut()) {
            case BOURSIER -> "Avec une moyenne estimée de " + moyenneTexte + "/20, tu es bien placé(e) pour une BOURSE en «"
                    + filiere.filiere() + "» à " + filiere.etablissement() + " (" + filiere.quotaBourse()
                    + " bourses). Chance estimée : " + pct + ".";
            case AIDE -> "Ta moyenne estimée de " + moyenneTexte + "/20 te situe plutôt sur une place d'AIDE/FPP en «"
                    + filiere.filiere() + "» (" + filiere.quotaAideFpp() + " places d'aide). Chance estimée : " + pct + ".";
            case PAYANT, PAYANT_UNIQUEMENT -> "Avec " + moyenneTexte + "/20, une sélection en boursier/aide est peu probable en «"
                    + filiere.filiere() + "» ; regarde les alternatives à meilleures cotes.";
            case CONCOURS -> "«" + filiere.filiere() + "» recrute par CONCOURS, pas au classement : prépare l'épreuve.";
        };
        String debouches = filiere.debouches().isEmpty() ? "" : " Débouchés : "
                + String.join(", ", filiere.debouches().subList(0, Math.min(2, filiere.debouches().size()))) + ".";
        return base + debouches + " " + DISCLAIMER;
    }
}
