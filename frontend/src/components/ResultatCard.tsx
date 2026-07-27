import type { Recommandation } from "../types";
import { ChanceMeter } from "./ChanceMeter";

/** Carte d'une filière recommandée : rang, filière, jauge de chance, argumentaire, quotas. */
export function ResultatCard({ reco, rang }: { reco: Recommandation; rang: number }) {
  const f = reco.filiere;
  return (
    <article className="panel reco">
      <div className="reco__rank" aria-hidden="true">
        {rang}
      </div>
      <div>
        <h3 className="reco__filiere">{f.filiere}</h3>
        <p className="reco__etab">
          {f.etablissement} · {f.universite}
        </p>
        <ChanceMeter proba={reco.proba} />
        <p className="reco__arg">{reco.argumentaire}</p>
        <div className="reco__meta">
          <span>
            Moyenne estimée <b>{reco.moyenne.toFixed(2)}</b>/20
          </span>
          <span>
            Bourses <b>{f.quotaBourse}</b>
          </span>
          <span>
            Aide/FPP <b>{f.quotaAideFpp}</b>
          </span>
        </div>
      </div>
    </article>
  );
}
