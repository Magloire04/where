import type { Probabilites } from "../types";
import { classeStatut, pourcentage } from "../utils/statut";

const R = 38;
const C = 2 * Math.PI * R;

/** Jauge circulaire (donut) affichant la chance estimée, colorée selon le statut. */
export function Gauge({ proba }: { proba: Probabilites }) {
  const classe = classeStatut(proba.statut);
  const offset = C * (1 - proba.pctAffiche);
  return (
    <div className="gauge">
      <svg
        className="gauge__svg"
        viewBox="0 0 88 88"
        role="img"
        aria-label={`Chance estimée : ${pourcentage(proba.pctAffiche)}`}
      >
        <circle className="gauge__track" cx="44" cy="44" r={R} />
        <circle
          className={`gauge__arc gauge__arc--${classe}`}
          cx="44"
          cy="44"
          r={R}
          style={{ strokeDasharray: C, strokeDashoffset: offset }}
        />
      </svg>
      <div className="gauge__center">
        <div className="gauge__pct">
          {Math.round(proba.pctAffiche * 100)}
          <small>%</small>
        </div>
      </div>
    </div>
  );
}
