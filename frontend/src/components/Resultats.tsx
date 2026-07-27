import type { Filiere, RecommandationResponse } from "../types";
import { ResultatCard } from "./ResultatCard";

/** Écran des résultats : top-3, alternatives, listes à part, disclaimer. */
export function Resultats({ data }: { data: RecommandationResponse }) {
  return (
    <section aria-label="Résultats">
      <div className="results-head">
        <h2>Tes meilleures chances</h2>
      </div>

      {data.top3.length === 0 ? (
        <div className="panel">
          <p>
            Aucune filière calculable avec ces matières. Vérifie ta série et complète les matières
            demandées (Maths, PCT, SVT…).
          </p>
        </div>
      ) : (
        data.top3.map((reco, i) => (
          <ResultatCard key={reco.filiere.filiere + i} reco={reco} rang={i + 1} />
        ))
      )}

      {data.alternatives.length > 0 && (
        <>
          <p className="section-label">Autres options intéressantes</p>
          {data.alternatives.map((reco, i) => (
            <ResultatCard key={`alt-${i}`} reco={reco} rang={i + 4} />
          ))}
        </>
      )}

      <ListeFilieres
        titre="Filières à concours (non estimables au classement)"
        filieres={data.concours}
      />
      <ListeFilieres titre="Filières entièrement payantes (sans bourse)" filieres={data.payantes} />
      <ListeFilieres
        titre="Données insuffisantes (matières manquantes)"
        filieres={data.donneesInsuffisantes}
      />

      <p className="disclaimer">
        Estimation indicative, pas une garantie de sélection. Le classement réel dépend de tes
        moyennes exactes et du nombre de candidats par filière.
      </p>
    </section>
  );
}

function ListeFilieres({ titre, filieres }: { titre: string; filieres: Filiere[] }) {
  if (filieres.length === 0) return null;
  return (
    <>
      <p className="section-label">{titre}</p>
      <div className="chiplist">
        {filieres.slice(0, 24).map((f, i) => (
          <span className="chip" key={`${f.filiere}-${i}`}>
            {f.filiere} <small>· {f.etablissement}</small>
          </span>
        ))}
      </div>
    </>
  );
}
