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
