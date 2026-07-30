import type { Filiere, RecommandationResponse } from "../types";
import { ResultatCard } from "./ResultatCard";

/** Liste compacte de filières (concours / à compléter) — ne masque jamais les options accessibles. */
function ListeFilieres({
  titre,
  note,
  filieres,
}: {
  titre: string;
  note: string;
  filieres: Filiere[];
}) {
  if (filieres.length === 0) {
    return null;
  }
  return (
    <>
      <p className="results__sep">{titre}</p>
      <ul className="mini">
        {filieres.map((f, i) => (
          <li className="mini__row" key={f.filiere + i}>
            <div className="mini__id">
              <div className="mini__title">{f.filiere}</div>
              <div className="mini__etab">
                {f.etablissement} · {f.universite}
              </div>
            </div>
            <span className="mini__tag">{note}</span>
          </li>
        ))}
      </ul>
    </>
  );
}

/** Écran des résultats : top-3 + alternatives, puis filières sur concours et à compléter. */
export function Resultats({ data }: { data: RecommandationResponse }) {
  return (
    <section className="results" aria-label="Résultats">
      <h2 className="results__title">Tes meilleures chances</h2>
      <p className="results__lead">
        Classées par chance d'obtenir une allocation — bourse ou aide.
      </p>

      {data.top3.length === 0 ? (
        <div className="card empty">
          Aucune filière entièrement calculable avec les notes saisies. Complète les matières
          demandées (avec leur coefficient) — les filières concernées, et celles recrutant sur
          concours, sont listées ci-dessous.
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

      <ListeFilieres
        titre="À compléter pour être estimées"
        note="Complète tes notes"
        filieres={data.donneesInsuffisantes}
      />
      <ListeFilieres
        titre="Accessibles sur concours"
        note="Recrute par concours"
        filieres={data.concours}
      />
    </section>
  );
}
