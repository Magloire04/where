package bj.orientation.model;

import java.util.List;

public enum Serie {
    A1, A2, B, C, D, E, F1, F2, F3, F4, G1, G2, G3, DT, DEAT, EA;

    /** Séries de l'enseignement général (seules proposées à la saisie). */
    private static final List<Serie> GENERALES = List.of(A1, A2, B, C, D);

    public static List<Serie> generales() {
        return GENERALES;
    }

    public static Serie fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("série nulle");
        }
        return Serie.valueOf(code.trim().toUpperCase());
    }
}
