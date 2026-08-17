import { useEffect, useState } from "react";
import { chargerStats } from "../api/client";
import type { Point, Stats } from "../types";

const CLE_TOKEN = "admin_token";

/** Durée écoulée depuis une date ISO, format compact (j / h / min). */
function depuis(iso: string): string {
  const ms = Date.now() - new Date(iso).getTime();
  if (!Number.isFinite(ms) || ms < 0) {
    return "-";
  }
  const minutes = Math.floor(ms / 60000);
  const heures = Math.floor(minutes / 60);
  const jours = Math.floor(heures / 24);
  if (jours >= 1) {
    return `${jours} j ${heures % 24} h`;
  }
  if (heures >= 1) {
    return `${heures} h ${minutes % 60} min`;
  }
  return `${minutes} min`;
}

function Kpi({ valeur, label }: { valeur: number | string; label: string }) {
  return (
    <div className="kpi">
      <span className="kpi__val">{valeur}</span>
      <span className="kpi__lab">{label}</span>
    </div>
  );
}

/** Barres verticales (calculs par jour). Une seule série -> teinte unique de la marque. */
function BarresJour({ points }: { points: Point[] }) {
  if (points.length === 0) {
    return <p className="admin__vide">Aucune donnée pour l'instant.</p>;
  }
  const max = Math.max(...points.map((p) => p.valeur), 1);
  return (
    <div className="chart-v">
      {points.map((p) => (
        <div className="chart-v__col" key={p.cle} title={`${p.cle} : ${p.valeur}`}>
          <span className="chart-v__val">{p.valeur}</span>
          <div
            className="chart-v__bar"
            style={{ height: `${Math.max(2, (p.valeur / max) * 100)}%` }}
          />
          <span className="chart-v__lab">{p.cle.slice(5)}</span>
        </div>
      ))}
    </div>
  );
}

/** Barres horizontales (répartition par série). */
function BarresSerie({ points }: { points: Point[] }) {
  if (points.length === 0) {
    return <p className="admin__vide">Aucune donnée pour l'instant.</p>;
  }
  const max = Math.max(...points.map((p) => p.valeur), 1);
  return (
    <div className="chart-h">
      {points.map((p) => (
        <div className="chart-h__row" key={p.cle}>
          <span className="chart-h__lab">{p.cle}</span>
          <div className="chart-h__track">
            <div
              className="chart-h__fill"
              style={{ width: `${Math.max(2, (p.valeur / max) * 100)}%` }}
            />
          </div>
          <span className="chart-h__val">{p.valeur}</span>
        </div>
      ))}
    </div>
  );
}

export function Admin() {
  const [token, setToken] = useState<string>(() => sessionStorage.getItem(CLE_TOKEN) ?? "");
  const [saisie, setSaisie] = useState("");
  const [stats, setStats] = useState<Stats | null>(null);
  const [erreur, setErreur] = useState<string | null>(null);
  const [chargement, setChargement] = useState(false);

  useEffect(() => {
    if (!token) {
      return;
    }
    setChargement(true);
    setErreur(null);
    chargerStats(token)
      .then(setStats)
      .catch(() => {
        setErreur("Jeton invalide ou accès refusé.");
        sessionStorage.removeItem(CLE_TOKEN);
        setToken("");
        setStats(null);
      })
      .finally(() => setChargement(false));
  }, [token]);

  function connecter() {
    const t = saisie.trim();
    if (t) {
      sessionStorage.setItem(CLE_TOKEN, t);
      setToken(t);
    }
  }

  if (!token || !stats) {
    return (
      <div className="admin">
        <h1>Monitoring</h1>
        <p className="admin__intro">Accès réservé. Saisis le jeton d'administration.</p>
        <div className="admin__login">
          <input
            type="password"
            aria-label="Jeton d'administration"
            placeholder="Jeton admin"
            value={saisie}
            onChange={(e) => setSaisie(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                connecter();
              }
            }}
          />
          <button
            type="button"
            className="btn btn--primary"
            onClick={connecter}
            disabled={chargement}
          >
            {chargement ? "…" : "Entrer"}
          </button>
        </div>
        {erreur && (
          <div className="alert alert--error" role="alert">
            {erreur}
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="admin">
      <h1>Monitoring</h1>
      <div className="kpis">
        <Kpi valeur={stats.totalCalculs} label="Calculs" />
        <Kpi valeur={stats.totalVisites} label="Visites" />
        <Kpi valeur={`${stats.latenceMoyenneMs} ms`} label="Latence moyenne" />
        <Kpi valeur={`${stats.latenceMaxMs} ms`} label="Latence max" />
        <Kpi valeur={`${(stats.tauxErreur * 100).toFixed(1)} %`} label="Taux d'erreur" />
        <Kpi valeur={depuis(stats.demarrage)} label="En ligne depuis" />
      </div>

      <h2 className="admin__h2">Calculs par jour</h2>
      <BarresJour points={stats.parJour} />

      <h2 className="admin__h2">Répartition par série</h2>
      <BarresSerie points={stats.parSerie} />
    </div>
  );
}
