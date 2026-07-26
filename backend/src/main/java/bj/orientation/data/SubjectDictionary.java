package bj.orientation.data;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;

/** Normalise les libellés de matières (relevé + guide) vers un code canonique. */
@Component
public class SubjectDictionary {
    private final Map<String, String> table = new LinkedHashMap<>();

    public SubjectDictionary() {
        put("MATHS", "maths", "mathematiques", "mathematique", "math");
        put("PCT", "pct", "spct", "sciences physiques", "physique chimie", "physique-chimie",
                "sciences physiques chimie et technologie");
        put("SVT", "svt", "sciences de la vie et de la terre");
        put("FR", "fr", "francais", "lettres");
        put("PHILO", "philo", "philosophie");
        put("HG", "hg", "hist-geo", "histoire-geographie", "histoire geographie", "hist geo");
        put("ANG", "ang", "anglais", "anglais lv1", "anglais (lv1)");
        put("ANG2", "anglais lv2", "anglais (lv2)");
        put("ESP", "esp", "espagnol", "espagnol (lv1)");
        put("ALL", "all", "allemand", "allemand (lv1)");
        put("ECO", "eco", "economie");
        put("EDC", "edc", "etude de cas", "etude de cas (g)");
        put("CG", "cg", "culture generale");
    }

    private void put(String canonique, String... formes) {
        for (String forme : formes) {
            table.put(normaliser(forme), canonique);
        }
    }

    public String canonique(String libelle) {
        if (libelle == null) {
            return null;
        }
        return table.get(normaliser(libelle));
    }

    static String normaliser(String valeur) {
        String sansAccent = Normalizer.normalize(valeur, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sansAccent.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
