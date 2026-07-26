package bj.orientation.data;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Détermine si une filière accepte une série donnée (correspondance par token exact). */
@Component
public class SerieMatcher {

    public boolean accepte(String seriesBacRaw, String serieCode) {
        if (seriesBacRaw == null || serieCode == null) {
            return false;
        }
        Set<String> tokens = tokens(seriesBacRaw);
        String code = serieCode.trim().toUpperCase();
        return tokens.contains(code);
    }

    private Set<String> tokens(String raw) {
        String[] parts = raw.toUpperCase().split("[^A-Z0-9]+");
        return new HashSet<>(Arrays.asList(parts));
    }
}
