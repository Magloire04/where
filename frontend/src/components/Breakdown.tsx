import type { Probabilites } from "../types";
import { pourcentage } from "../utils/statut";

/** Barre à 3 segments (boursier / aide / payant) + légende. */
export function Breakdown({ proba }: { proba: Probabilites }) {
  const segments = [
    {
      cle: "boursier" as const,
      valeur: proba.pBourse,
      label: "Boursier",
      couleur: "var(--boursier)",
    },
    { cle: "aide" as const, valeur: proba.pAide, label: "Aide", couleur: "var(--aide)" },
    { cle: "payant" as const, valeur: proba.pPayant, label: "Payant", couleur: "var(--payant)" },
  ];
  return (
    <div>
      <div
        className="split"
        role="img"
        aria-label={`Répartition : boursier ${pourcentage(proba.pBourse)}, aide ${pourcentage(
          proba.pAide,
        )}, payant ${pourcentage(proba.pPayant)}`}
      >
        {segments.map((s) => (
          <span
            key={s.cle}
            className={`split__seg--${s.cle}`}
            style={{ width: `${s.valeur * 100}%` }}
          />
        ))}
      </div>
      <div className="legend">
        {segments.map((s) => (
          <span key={s.cle}>
            <i className="dot" style={{ background: s.couleur }} /> {s.label}{" "}
            {pourcentage(s.valeur)}
          </span>
        ))}
      </div>
    </div>
  );
}
