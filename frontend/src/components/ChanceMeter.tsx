import type { Probabilites } from "../types";
import { classeStatut, libelleStatut, pourcentage } from "../utils/statut";

/** Jauge de chance : barre à 3 segments (boursier / aide / payant) + statut dominant. */
export function ChanceMeter({ proba }: { proba: Probabilites }) {
  const segments = [
    { cle: "boursier" as const, valeur: proba.pBourse },
    { cle: "aide" as const, valeur: proba.pAide },
    { cle: "payant" as const, valeur: proba.pPayant },
  ];
  return (
    <div className="meter">
      <div className="meter__head">
        <span className={`badge badge--${classeStatut(proba.statut)}`}>
          {libelleStatut(proba.statut)}
        </span>
        <span className="meter__pct">{pourcentage(proba.pctAffiche)}</span>
      </div>
      <div
        className="meter__bar"
        role="img"
        aria-label={`Chances estimées : boursier ${pourcentage(proba.pBourse)}, aide ${pourcentage(
          proba.pAide,
        )}, payant ${pourcentage(proba.pPayant)}`}
      >
        {segments.map((s) => (
          <div
            key={s.cle}
            className={`meter__seg meter__seg--${s.cle}`}
            style={{ width: `${s.valeur * 100}%` }}
          />
        ))}
      </div>
      <div className="meter__legend">
        <span>
          <i className="dot" style={{ background: "var(--boursier)" }} /> Boursier{" "}
          {pourcentage(proba.pBourse)}
        </span>
        <span>
          <i className="dot" style={{ background: "var(--aide)" }} /> Aide{" "}
          {pourcentage(proba.pAide)}
        </span>
        <span>
          <i className="dot" style={{ background: "var(--payant)" }} /> Payant{" "}
          {pourcentage(proba.pPayant)}
        </span>
      </div>
    </div>
  );
}
