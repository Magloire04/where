package bj.orientation.data;

import bj.orientation.model.Filiere;
import bj.orientation.model.ModeEntree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

/** Charge, au démarrage, les filières publiques depuis les ressources JSON du guide. */
@Repository
public class FiliereRepository {
    private static final String[] FICHIERS = {
        "uac.json", "parakou.json", "unstim.json", "una.json", "autres_publics.json"
    };
    private final List<Filiere> filieres;

    public FiliereRepository() {
        this.filieres = charger();
    }

    public List<Filiere> toutes() {
        return filieres;
    }

    private List<Filiere> charger() {
        ObjectMapper mapper = new ObjectMapper();
        List<Filiere> resultat = new ArrayList<>();
        for (String fichier : FICHIERS) {
            try (InputStream in = getClass().getResourceAsStream("/data/" + fichier)) {
                if (in == null) {
                    throw new IllegalStateException("Ressource absente: /data/" + fichier);
                }
                JsonNode racine = mapper.readTree(in);
                String universite = racine.path("universite").asText(racine.path("groupe").asText(""));
                for (JsonNode noeud : racine.path("filieres")) {
                    List<String> debouches = new ArrayList<>();
                    noeud.path("debouches").forEach(d -> debouches.add(d.asText()));
                    resultat.add(new Filiere(
                        noeud.path("num").asInt(),
                        universite,
                        noeud.path("etablissement").asText(""),
                        noeud.path("filiere").asText(""),
                        noeud.path("quota_bourse").asInt(0),
                        noeud.path("quota_aide_fpp").asInt(0),
                        ModeEntree.parse(noeud.path("mode_entree").asText("Classement")),
                        noeud.path("series_bac_raw").asText(""),
                        noeud.path("matieres_raw").asText(""),
                        debouches,
                        noeud.path("page").asInt(0)));
                }
            } catch (Exception e) {
                throw new IllegalStateException("Échec chargement " + fichier, e);
            }
        }
        return resultat;
    }
}
