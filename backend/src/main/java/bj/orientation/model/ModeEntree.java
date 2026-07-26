package bj.orientation.model;

public enum ModeEntree {
    CLASSEMENT, CONCOURS, PAYANT;

    public static ModeEntree parse(String raw) {
        if (raw == null) {
            return CLASSEMENT;
        }
        String valeur = raw.trim().toLowerCase();
        if (valeur.startsWith("concours")) {
            return CONCOURS;
        }
        if (valeur.startsWith("a titre payant") || valeur.startsWith("à titre payant")) {
            return PAYANT;
        }
        return CLASSEMENT;
    }
}
