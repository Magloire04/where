import { useEffect, useState } from "react";
import { chargerMatieres, chargerSeries, recommander } from "./api/client";
import { Footer } from "./components/Footer";
import { NotesTable } from "./components/NotesTable";
import { Resultats } from "./components/Resultats";
import { Aide } from "./pages/Aide";
import { Cgu } from "./pages/Cgu";
import { Confidentialite } from "./pages/Confidentialite";
import { MentionsLegales } from "./pages/MentionsLegales";
import { libelleMatiere } from "./utils/matieres";
import type { MatiereSerie, NoteSaisie, RangNote, RecommandationResponse } from "./types";

type Route = "accueil" | "aide" | "confidentialite" | "cgu" | "mentions-legales";

function routeFromHash(): Route {
  const h = window.location.hash.replace(/^#\/?/, "");
  if (h === "aide" || h === "confidentialite" || h === "cgu" || h === "mentions-legales") {
    return h;
  }
  return "accueil";
}

const NB_MATIERES_FORTES = 3;
const lignesVides = (n: number): RangNote[] =>
  Array.from({ length: n }, () => ({ libelle: "", note: "", coefficient: "" }));

function notesValides(lignes: RangNote[]): NoteSaisie[] {
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

/** Construit les lignes « à compléter » depuis les codes renvoyés, en gardant les notes déjà tapées. */
function construireCompletion(
  codes: string[],
  options: MatiereSerie[],
  existantes: RangNote[],
): RangNote[] {
  return codes.map((code) => {
    const opt = options.find((o) => o.code === code);
    const libelle = opt?.libelle ?? libelleMatiere(code);
    const deja = existantes.find((l) => l.libelle === libelle);
    return (
      deja ?? {
        libelle,
        note: "",
        coefficient: opt && opt.coefficient != null ? String(opt.coefficient) : "",
      }
    );
  });
}

function App() {
  const [series, setSeries] = useState<string[]>([]);
  const [serie, setSerie] = useState("");
  const [options, setOptions] = useState<MatiereSerie[]>([]);
  const [lignesFortes, setLignesFortes] = useState<RangNote[]>([]);
  const [lignesCompletion, setLignesCompletion] = useState<RangNote[]>([]);
  const [aCompleter, setACompleter] = useState<string[]>([]);
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [resultats, setResultats] = useState<RecommandationResponse | null>(null);
  const [route, setRoute] = useState<Route>(routeFromHash());

  useEffect(() => {
    chargerSeries()
      .then(setSeries)
      .catch(() => setSeries([]));
  }, []);

  useEffect(() => {
    const onHash = () => {
      setRoute(routeFromHash());
      window.scrollTo(0, 0);
    };
    window.addEventListener("hashchange", onHash);
    return () => window.removeEventListener("hashchange", onHash);
  }, []);

  async function choisirSerie(nouvelleSerie: string) {
    setSerie(nouvelleSerie);
    setResultats(null);
    setErreur(null);
    setLignesFortes(lignesVides(NB_MATIERES_FORTES));
    setLignesCompletion([]);
    setACompleter([]);
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

  async function lancer(lignesUtilisees: RangNote[]) {
    setErreur(null);
    if (!serie) {
      setErreur("Choisis ta série de bac.");
      return;
    }
    if (notesValides(lignesFortes).length < 2) {
      setErreur(
        "Choisis au moins 2 de tes matières fortes, avec leur note (0 à 20) et leur coefficient.",
      );
      return;
    }
    const notes = notesValides(lignesUtilisees);
    const matieresFortes = lignesFortes.map((l) => l.libelle.trim()).filter(Boolean);
    setChargement(true);
    try {
      const reponse = await recommander(serie, notes, matieresFortes);
      setResultats(reponse);
      setACompleter(reponse.matieresACompleter);
      setLignesCompletion((prev) =>
        construireCompletion(reponse.matieresACompleter, options, prev),
      );
    } catch (e) {
      setErreur(e instanceof Error ? e.message : "Erreur lors du calcul.");
    } finally {
      setChargement(false);
    }
  }

  const optionsCompletion = options.filter((o) => aCompleter.includes(o.code));

  if (route !== "accueil") {
    const contenu =
      route === "aide" ? (
        <Aide />
      ) : route === "confidentialite" ? (
        <Confidentialite />
      ) : route === "cgu" ? (
        <Cgu />
      ) : (
        <MentionsLegales />
      );
    return (
      <>
        <main className="container legal-page">
          <a className="legal__back" href="#/">
            ← Retour à l'accueil
          </a>
          {contenu}
        </main>
        <Footer />
      </>
    );
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
            filières qui les valorisent, chacune avec ta chance d'allocation (bourse ou aide).
          </p>
          <a className="hero__aide" href="#/aide">
            Comment ça marche ?
          </a>
        </div>
      </header>

      <main className="container">
        <section className="card card--float" aria-label="Ton profil">
          <div className="field">
            <label htmlFor="serie" className="field__lab">
              <span className="step" aria-hidden="true">
                1
              </span>{" "}
              Ta série (enseignement général)
            </label>
            <select
              id="serie"
              className="select-serie"
              value={serie}
              onChange={(e) => choisirSerie(e.target.value)}
            >
              <option value="">Choisis ta série</option>
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
                lignes={lignesFortes}
                onChange={setLignesFortes}
                options={options}
                max={NB_MATIERES_FORTES}
              />
              <p className="hint">
                Choisis, parmi les matières de la série {serie}, les{" "}
                <b>3 où tu as tes meilleures notes</b> (avec leur coefficient, déjà rempli pour les
                séries C et D).
              </p>
            </div>
          )}

          <div className="actions">
            <button
              type="button"
              className="btn btn--primary btn--lg"
              onClick={() => lancer(lignesFortes)}
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

        {resultats && aCompleter.length > 0 && (
          <section className="card card--float" aria-label="Affiner l'estimation">
            <div className="field">
              <label className="field__lab">
                <span className="step" aria-hidden="true">
                  +
                </span>{" "}
                Affine ton estimation
              </label>
              <p className="hint">
                Certaines filières se calculent aussi sur ces matières. Renseigne-les pour les faire
                apparaître, calculées sur leurs 3 matières exactes.
              </p>
              <NotesTable
                lignes={lignesCompletion}
                onChange={setLignesCompletion}
                options={optionsCompletion}
                max={optionsCompletion.length}
              />
            </div>
            <div className="actions">
              <button
                type="button"
                className="btn btn--primary btn--block"
                onClick={() => lancer([...lignesFortes, ...lignesCompletion])}
                disabled={chargement}
              >
                {chargement ? "Calcul…" : "Recalculer"}
              </button>
            </div>
          </section>
        )}

        {resultats && <Resultats data={resultats} />}
      </main>

      <Footer />
    </>
  );
}

export default App;
