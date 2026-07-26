package bj.orientation.model;

public enum Serie {
    A1, A2, B, C, D, E, F1, F2, F3, F4, G1, G2, G3, DT, DEAT, EA;

    public static Serie fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("série nulle");
        }
        return Serie.valueOf(code.trim().toUpperCase());
    }
}
