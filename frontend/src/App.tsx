import { useEffect, useState } from "react";
import { chargerMatieres, chargerSeries, recommander } from "./api/client";
import { NotesTable } from "./components/NotesTable";
import { Resultats } from "./components/Resultats";
import type { MatiereSerie, NoteSaisie, RangNote, RecommandationResponse } from "./types";

const NB_MATIERES_FORTES = 3;
const lignesVides = (n: number): RangNote[] =>
  Array.from({ length: n }, () => ({ libelle: "", note: "", coefficient: "" }));

function App() {
  const [series, setSeries] = useState<string[]>([]);
  const [serie, setSerie] = useState("");
  const [options, setOptions] = useState<MatiereSerie[]>([]);
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
    setLignes(lignesVides(NB_MATIERES_FORTES));
    if (!nouvelleSerie) {
      setOptions([]);
      return;
    }
    try {
      setOptions(await chargerMatieres(nouvelleSerie));
    } catch {
      setOptions([]);
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
    if (notes.length < 2) {
      setErreur(
        "Choisis au moins 2 de tes matières fortes, avec leur note (0–20) et leur coefficient.",
      );
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
            Tes <em>3 matières fortes</em> te montrent où tu as le plus de chances d'être boursier
            ou aidé.
          </h1>
          <p className="hero__sub">
            Choisis ta série, puis tes 3 matières les plus fortes avec leurs notes. On te liste les
            filières qui calculent leur classement sur ces matières, chacune avec ta chance
            d'allocation (bourse ou aide).
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
                Tes 3 matières les plus fortes
              </label>
              <NotesTable
                lignes={lignes}
                onChange={setLignes}
                options={options}
                max={NB_MATIERES_FORTES}
              />
              <p className="hint">
                Choisis, parmi les matières de la série {serie}, les{" "}
                <b>3 où tu as tes meilleures notes</b> (avec leur coefficient — déjà rempli pour les
                séries C et D). On liste les filières dont le calcul retient au moins 2 de ces
                matières.
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
          Données : Guide d'orientation MESRS 2026-2027 (224 filières publiques). Aucune donnée
          personnelle n'est conservée : tes notes sont traitées le temps du calcul, puis oubliées.
        </div>
      </footer>
    </>
  );
}

export default App;
