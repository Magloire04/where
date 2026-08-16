import type { CategorieEtab } from "../utils/etablissement";
import type { Filiere, Recommandation, RecommandationResponse } from "../types";
import { categorieEtablissement, libelleCategorie, ORDRE_CATEGORIES } from "../utils/etablissement";
import { ResultatCard } from "./ResultatCard";

interface Classee {
  reco: Recommandation;
  rang: number;
}

/** Liste compacte de filières sur concours (accessibles mais non calculées ainsi). */
function ListeFilieres({ filieres }: { filieres: Filiere[] }) {
  if (filieres.length === 0) {
    return null;
  }
  return (
    <>
      <p className="results__sep">Accessibles sur concours</p>
      <ul className="mini">
        {filieres.map((f, i) => (
          <li className="mini__row" key={f.filiere + i}>
            <div className="mini__id">
              <div className="mini__title">{f.filiere}</div>
              <div className="mini__etab">
                {f.etablissement} · {f.universite}
              </div>
            </div>
            <span className="mini__tag">Recrute par concours</span>
          </li>
        ))}
      </ul>
    </>
  );
}

/** Écran des résultats : top-3 global, puis sections par catégorie (Facultés → Écoles → …). */
export function Resultats({ data }: { data: RecommandationResponse }) {
  const classees: Classee[] = data.recommandations.map((reco, i) => ({ reco, rang: i + 1 }));
  const top3 = classees.slice(0, 3);
  const parCategorie = new Map<CategorieEtab, Classee[]>();
  for (const c of classees) {
    const cat = categorieEtablissement(c.reco.filiere.etablissement);
    const liste = parCategorie.get(cat) ?? [];
    if (!parCategorie.has(cat)) {
      parCategorie.set(cat, liste);
    }
    liste.push(c);
  }

  return (
    <section className="results" aria-label="Résultats">
      <div className="disclaimer" role="note">
        <span className="disclaimer__icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none">
            <path
              d="M12 3.5 4 7v5c0 4.4 3.2 7.6 8 8.5 4.8-.9 8-4.1 8-8.5V7l-8-3.5Z"
              stroke="currentColor"
              strokeWidth="1.6"
              strokeLinejoin="round"
            />
            <path
              d="M12 8.2v4.2M12 15.4h.01"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
            />
          </svg>
        </span>
        <p className="disclaimer__txt">
          <b>Estimation indicative.</b> Ces résultats ne constituent en aucun cas une garantie de
          classement par l'État. Ce sont des propositions calculées à partir de tes notes et des
          conditions de classement du guide d'orientation officiel du MESRS.
        </p>
      </div>

      <h2 className="results__title">Tes meilleures chances</h2>
      <p className="results__lead">
        Classées par chance d'obtenir une allocation — bourse ou aide.
      </p>

      {classees.length === 0 ? (
        <div className="card empty">
          Aucune filière calculée pour l'instant. Renseigne tes 3 matières fortes — et complète les
          matières demandées — pour voir tes chances.
        </div>
      ) : (
        <>
          {top3.map((c) => (
            <ResultatCard key={`top-${c.rang}`} reco={c.reco} rang={c.rang} />
          ))}

          {ORDRE_CATEGORIES.map((cat) => {
            const items = parCategorie.get(cat);
            if (!items || items.length === 0) {
              return null;
            }
            return (
              <div key={cat}>
                <p className="results__sep">
                  {libelleCategorie(cat)} · {items.length}
                </p>
                {items.map((c) => (
                  <ResultatCard key={`${cat}-${c.rang}`} reco={c.reco} rang={c.rang} />
                ))}
              </div>
            );
          })}
        </>
      )}

      <ListeFilieres filieres={data.concours} />
    </section>
  );
}
