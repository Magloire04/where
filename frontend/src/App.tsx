import { useEffect, useState } from "react";
import { chargerMatieres, chargerSeries, recommander } from "./api/client";
import { NotesTable } from "./components/NotesTable";
import { Resultats } from "./components/Resultats";
import type { NoteSaisie, RangNote, RecommandationResponse } from "./types";

function App() {
  const [series, setSeries] = useState<string[]>([]);
  const [serie, setSerie] = useState("");
  const [lignes, setLignes] = useState<RangNote[]>([]);
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [resultats, setResultats] = useState<RecommandationResponse | null>(null);

  useEffect(() => {
    chargerSeries()
      .then(setSeries)
      .catch(() => setSeries([]));
  }, []);

  async function choisirSerie(nouvelleSerie: string) {
    setSerie(nouvelleSerie);
    setResultats(null);
    setErreur(null);
    if (!nouvelleSerie) {
      setLignes([]);
      return;
    }
    try {
      const matieres = await chargerMatieres(nouvelleSerie);
      setLignes(
        matieres.map((m) => ({
          libelle: m.libelle,
          note: "",
          coefficient: m.coefficient != null ? String(m.coefficient) : "",
        })),
      );
    } catch {
      setLignes([{ libelle: "", note: "", coefficient: "" }]);
    }
  }

  function notesValides(): NoteSaisie[] {
    return lignes
      .map((l) => ({
        libelle: l.libelle.trim(),
        note: Number(l.note),
        coefficient: Number(l.coefficient),
      }))
      .filter(
        (n) =>
          n.libelle !== "" &&
          Number.isFinite(n.note) &&
          n.note >= 0 &&
          n.note <= 20 &&
          Number.isFinite(n.coefficient) &&
          n.coefficient > 0,
      );
  }

  async function calculer() {
    setErreur(null);
    if (!serie) {
      setErreur("Choisis ta série de bac.");
      return;
    }
    const notes = notesValides();
    if (notes.length === 0) {
      setErreur("Saisis au moins une note (0–20) avec son coefficient.");
      return;
    }
    setChargement(true);
    try {
      setResultats(await recommander(serie, notes));
    } catch (e) {
      setErreur(e instanceof Error ? e.message : "Erreur lors du calcul.");
    } finally {
      setChargement(false);
    }
  }

  return (
    <>
      <header className="hero">
        <div className="hero__inner">
          <div className="brand">
            <span className="brand__dot" aria-hidden="true" /> Après mon bac
          </div>
          <h1 className="hero__title">
            Trouve la filière où tu as le plus de chances d'être <em>boursier</em>.
          </h1>
          <p className="hero__sub">
            Choisis ta série : les matières s'affichent automatiquement. Tu n'as plus qu'à saisir
            tes notes — on calcule ta moyenne de classement par filière et les 3 meilleures cotes.
          </p>
        </div>
      </header>

      <main className="container">
        <section className="card card--float" aria-label="Ton profil">
          <div className="field">
            <label htmlFor="serie" className="field__lab">
              <span className="step" aria-hidden="true">
                1
              </span>{" "}
              Ta série de bac
            </label>
            <select
              id="serie"
              className="select-serie"
              value={serie}
              onChange={(e) => choisirSerie(e.target.value)}
            >
              <option value="">— choisis ta série —</option>
              {series.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>

          {serie && (
            <div className="field">
              <label className="field__lab">
                <span className="step" aria-hidden="true">
                  2
                </span>{" "}
                Tes notes
              </label>
              <NotesTable lignes={lignes} onChange={setLignes} />
              <p className="hint">
                Les matières de la série {serie} sont pré-affichées : saisis chaque note /20. Les
                coefficients figurent sur ton relevé (déjà remplis pour les séries C et D).
              </p>
            </div>
          )}

          <div className="actions">
            <button
              type="button"
              className="btn btn--primary btn--lg"
              onClick={calculer}
              disabled={chargement || !serie}
            >
              {chargement ? "Calcul…" : "Voir mes chances"}
            </button>
          </div>
          {erreur && (
            <div className="alert alert--error" role="alert">
              {erreur}
            </div>
          )}
        </section>

        {resultats && <Resultats data={resultats} />}
      </main>

      <footer className="footer">
        <div className="footer__inner">
          Données : Guide d'orientation MESRS 2025-2026 (216 filières publiques). Aucune donnée
          personnelle n'est conservée : tes notes sont traitées le temps du calcul, puis oubliées.
        </div>
      </footer>
    </>
  );
}

export default App;
