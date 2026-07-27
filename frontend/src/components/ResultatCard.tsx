import type { Recommandation } from "../types";
import { classeStatut, libelleStatut } from "../utils/statut";
import { Breakdown } from "./Breakdown";
import { Gauge } from "./Gauge";

/** Carte d'une filière recommandée : rang, jauge, statut, répartition, argumentaire, quotas. */
export function ResultatCard({ reco, rang }: { reco: Recommandation; rang: number }) {
  const f = reco.filiere;
  const classe = classeStatut(reco.proba.statut);
  return (
    <article className={`card reco${rang === 1 ? " reco--top" : ""}`}>
      <div className="reco__head">
        <div className="reco__rank" aria-hidden="true">
          {rang}
        </div>
        <div className="reco__id">
          <div className="reco__title">{f.filiere}</div>
          <div className="reco__etab">
            {f.etablissement} · {f.universite}
          </div>
        </div>
        <Gauge proba={reco.proba} />
      </div>

      <div className="reco__statut">
        <span className={`pill pill--${classe}`}>{libelleStatut(reco.proba.statut)}</span>
      </div>

      <Breakdown proba={reco.proba} />

      <p className="reco__arg">{reco.argumentaire}</p>

      <div className="chips">
        <span className="chip">
          Moyenne <b>{reco.moyenne.toFixed(2)}</b>/20
        </span>
        <span className="chip">
          Bourses <b>{f.quotaBourse}</b>
        </span>
        <span className="chip">
          Aide/FPP <b>{f.quotaAideFpp}</b>
        </span>
      </div>
    </article>
  );
}
