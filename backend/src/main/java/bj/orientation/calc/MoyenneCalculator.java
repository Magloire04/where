package bj.orientation.calc;

import bj.orientation.model.MatiereNote;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Calcule la moyenne de classement pondérée sur les 3 matières d'une filière. */
@Component
public class MoyenneCalculator {

    public OptionalDouble calculer(List<String> matieresCanoniques, List<MatiereNote> notes) {
        if (matieresCanoniques == null || matieresCanoniques.isEmpty()) {
            return OptionalDouble.empty();
        }
        Map<String, MatiereNote> parCode = notes.stream()
            .collect(Collectors.toMap(MatiereNote::canonique, Function.identity(), (a, b) -> a));
        double sommeNoteCoef = 0;
        double sommeCoef = 0;
        for (String matiere : matieresCanoniques) {
            MatiereNote note = parCode.get(matiere);
            if (note == null || note.coefficient() <= 0) {
                return OptionalDouble.empty();
            }
            sommeNoteCoef += note.note() * note.coefficient();
            sommeCoef += note.coefficient();
        }
        return OptionalDouble.of(sommeNoteCoef / sommeCoef);
    }
}
