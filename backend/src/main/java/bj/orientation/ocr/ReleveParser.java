package bj.orientation.ocr;

import bj.orientation.model.LigneReleve;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Extrait des lignes (matière, note, coefficient) depuis le texte OCR brut d'un relevé.
 * Heuristique : libellé = texte avant le premier nombre ; note = 1er nombre dans [0, 20] ;
 * coefficient = 1er entier de [1, 10] après la note. L'utilisateur corrige toujours ensuite.
 */
@Component
public class ReleveParser {
    private static final Pattern NOMBRE = Pattern.compile("\\d+(?:[.,]\\d+)?");

    public List<LigneReleve> parser(String texteOcr) {
        List<LigneReleve> lignes = new ArrayList<>();
        if (texteOcr == null || texteOcr.isBlank()) {
            return lignes;
        }
        for (String ligne : texteOcr.split("\\r?\\n")) {
            LigneReleve extraite = parserLigne(ligne);
            if (extraite != null) {
                lignes.add(extraite);
            }
        }
        return lignes;
    }

    private LigneReleve parserLigne(String ligne) {
        Matcher matcher = NOMBRE.matcher(ligne);
        List<Double> nombres = new ArrayList<>();
        int premierIndex = -1;
        while (matcher.find()) {
            if (premierIndex < 0) {
                premierIndex = matcher.start();
            }
            nombres.add(Double.parseDouble(matcher.group().replace(',', '.')));
        }
        if (premierIndex <= 0) {
            return null;
        }
        String libelle = ligne.substring(0, premierIndex).trim();
        if (libelle.isEmpty() || !libelle.matches(".*[A-Za-zÀ-ÿ].*")) {
            return null;
        }
        Double note = null;
        Double coefficient = null;
        for (Double nombre : nombres) {
            if (note == null && nombre >= 0 && nombre <= 20) {
                note = nombre;
            } else if (note != null
                && coefficient == null
                && nombre >= 1
                && nombre <= 10
                && nombre == Math.floor(nombre)) {
                coefficient = nombre;
            }
        }
        return note == null ? null : new LigneReleve(libelle, note, coefficient);
    }
}
