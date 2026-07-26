package bj.orientation.calc;

import bj.orientation.config.EstimateurProperties;
import bj.orientation.model.Filiere;
import bj.orientation.model.Palier;
import org.springframework.stereotype.Component;

/** Affecte à chaque filière un palier de sélectivité (spec §6). */
@Component
public class SelectiviteTiering {
    private final EstimateurProperties props;

    public SelectiviteTiering(EstimateurProperties props) {
        this.props = props;
    }

    public Palier palier(Filiere filiere) {
        boolean prestige = props.prestige().contains(filiere.filiere());
        int bourse = filiere.quotaBourse();
        int aide = filiere.quotaAideFpp();
        if (prestige) {
            return (bourse < 20 || aide == 0) ? Palier.T1 : Palier.T2;
        }
        if (aide >= 3 * Math.max(bourse, 1)) {
            return Palier.T4;
        }
        return Palier.T3;
    }
}
