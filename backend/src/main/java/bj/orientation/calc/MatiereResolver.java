package bj.orientation.calc;

import bj.orientation.data.SubjectDictionary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Résout, pour une série donnée, les 3 matières canoniques de calcul d'une filière
 * à partir du champ {@code matieres_raw} du guide. Renvoie une liste vide si non résoluble
 * (ex. "toutes les trois matières écrites" des DEAT, ou matières non précisées).
 */
@Component
public class MatiereResolver {
    private final SubjectDictionary dico;

    public MatiereResolver(SubjectDictionary dico) {
        this.dico = dico;
    }

    public List<String> resoudre(String matieresRaw, String serieCode) {
        if (matieresRaw == null || matieresRaw.isBlank()) {
            return List.of();
        }
        String code = serieCode.trim().toUpperCase();
        String clause = choisirClause(matieresRaw, code);
        if (clause == null
                || clause.toLowerCase().contains("toutes les trois")
                || clause.toLowerCase().contains("non précisé")) {
            return List.of();
        }
        List<String> resultat = new ArrayList<>();
        for (String segment : clause.split("[/,]")) {
            String canon = premierReconnu(segment);
            if (canon != null && !resultat.contains(canon)) {
                resultat.add(canon);
            }
            if (resultat.size() == 3) {
                break;
            }
        }
        return resultat;
    }

    private String choisirClause(String raw, String code) {
        String[] clauses = raw.split("\\|");
        String defaut = null;
        for (String clause : clauses) {
            String bas = clause.toLowerCase();
            int idx = bas.indexOf(':');
            if (idx > 0 && bas.substring(0, idx).contains("pour")) {
                String prefixe = " " + bas.substring(0, idx).toUpperCase().replaceAll("[^A-Z0-9]", " ") + " ";
                if (prefixe.contains(" " + code + " ")) {
                    return clause.substring(clause.indexOf(':') + 1);
                }
            } else if (defaut == null) {
                defaut = clause;
            }
        }
        return defaut != null ? defaut : (clauses.length > 0 ? clauses[0] : raw);
    }

    private String premierReconnu(String segment) {
        String canon = dico.canonique(nettoyer(segment));
        if (canon != null) {
            return canon;
        }
        for (String part : segment.split("(?i)\\bou\\b")) {
            canon = dico.canonique(nettoyer(part));
            if (canon != null) {
                return canon;
            }
        }
        return null;
    }

    private String nettoyer(String segment) {
        return segment.replaceAll("\\([^)]*\\)", " ").trim();
    }
}
