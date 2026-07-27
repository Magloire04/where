import { useEffect, useState } from "react";
import { chargerSeries, recommander } from "./api/client";
import { NotesTable } from "./components/NotesTable";
import { ReleveUpload } from "./components/ReleveUpload";
import { Resultats } from "./components/Resultats";
import type { NoteSaisie, RangNote, RecommandationResponse } from "./types";

const LIGNE_VIDE: RangNote = { libelle: "", note: "", coefficient: "" };

function App() {
  const [series, setSeries] = useState<string[]>([]);
  const [serie, setSerie] = useState("");
  const [mode, setMode] = useState<"manuel" | "upload">("manuel");
  const [lignes, setLignes] = useState<RangNote[]>([
    { ...LIGNE_VIDE },
    { ...LIGNE_VIDE },
    { ...LIGNE_VIDE },
  ]);
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [resultats, setResultats] = useState<RecommandationResponse | null>(null);

  useEffect(() => {
    chargerSeries()
      .then(setSeries)
      .catch(() => setSeries([]));
  }, []);

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
      setErreur("Ajoute au moins une matière avec une note (0–20) et un coefficient.");
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
      <header className="masthead">
        <div className="masthead__inner">
          <span className="masthead__title">Après mon bac</span>
          <span className="masthead__sub">Estime tes chances de bourse par filière — Bénin</span>
        </div>
      </header>

      <main className="container">
        <div className="intro">
          <h1>Quelle filière te donne le plus de chances d'être boursier&nbsp;?</h1>
          <p>
            Renseigne ta série et tes notes : on calcule ta moyenne de classement par filière et on
            te propose les 3 meilleures cotes, avec ton statut estimé (boursier, aide, payant).
          </p>
        </div>

        <section className="panel" aria-label="Ton profil">
          <div className="field">
            <label htmlFor="serie">Série du bac</label>
            <select
              id="serie"
              className="select-serie"
              value={serie}
              onChange={(e) => setSerie(e.target.value)}
            >
              <option value="">— choisis —</option>
              {series.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>

          <div className="field">
            <label>Tes notes</label>
            <div className="tabs" role="tablist" aria-label="Mode de saisie des notes">
              <button
                type="button"
                className="tab"
                role="tab"
                aria-selected={mode === "manuel"}
                onClick={() => setMode("manuel")}
              >
                Saisie manuelle
              </button>
              <button
                type="button"
                className="tab"
                role="tab"
                aria-selected={mode === "upload"}
                onClick={() => setMode("upload")}
              >
                Téléverser le relevé
              </button>
            </div>
            {mode === "upload" && (
              <ReleveUpload
                onExtrait={(nouvelles) => {
                  if (nouvelles.length > 0) setLignes(nouvelles);
                  setMode("manuel");
                }}
              />
            )}
            <NotesTable lignes={lignes} onChange={setLignes} />
            <p className="hint">
              Indique la note /20 et le coefficient de chaque matière (ils figurent sur ton relevé).
            </p>
          </div>

          <div className="actions">
            <button
              type="button"
              className="btn btn--primary"
              onClick={calculer}
              disabled={chargement}
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

      <footer className="site-footer">
        <div className="site-footer__inner">
          Données : Guide d'orientation MESRS 2025-2026 (216 filières publiques). Aucune donnée
          personnelle n'est conservée : tes notes et ton relevé sont traités le temps du calcul,
          puis oubliés.
        </div>
      </footer>
    </>
  );
}

export default App;
