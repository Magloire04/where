import type { RecommandationResponse } from "../types";
import { ResultatCard } from "./ResultatCard";

/** Écran des résultats : top-3 + alternatives. */
export function Resultats({ data }: { data: RecommandationResponse }) {
  return (
    <section className="results" aria-label="Résultats">
      <h2 className="results__title">Tes meilleures chances</h2>
      <p className="results__lead">
        Classées de la plus forte à la plus faible chance d'allocation.
      </p>

      {data.top3.length === 0 ? (
        <div className="card empty">
          Aucune filière calculable avec ces matières. Vérifie ta série et complète les matières
          demandées (Maths, PCT, SVT…).
        </div>
      ) : (
        data.top3.map((reco, i) => (
          <ResultatCard key={reco.filiere.filiere + i} reco={reco} rang={i + 1} />
        ))
      )}

      {data.alternatives.length > 0 && (
        <>
          <p className="results__sep">Autres options intéressantes</p>
          {data.alternatives.map((reco, i) => (
            <ResultatCard key={`alt-${i}`} reco={reco} rang={i + 4} />
          ))}
        </>
      )}
    </section>
  );
}
