package bj.orientation.model;

import java.util.List;

/** Une filière du guide d'orientation, avec ses quotas et critères. */
public record Filiere(
        int num,
        String universite,
        String etablissement,
        String filiere,
        int quotaBourse,
        int quotaAideFpp,
        ModeEntree modeEntree,
        String seriesBacRaw,
        String matieresRaw,
        List<String> debouches,
        int page) {
}
