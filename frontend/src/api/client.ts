import type { MatiereSerie, NoteSaisie, RecommandationResponse } from "../types";

const BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

interface Enveloppe<T> {
  data?: T;
  error?: { code: string; message: string; status: number };
}

async function lire<T>(reponse: Response): Promise<T> {
  const corps = (await reponse.json().catch(() => null)) as Enveloppe<T> | null;
  if (!reponse.ok || !corps || corps.error) {
    throw new Error(corps?.error?.message ?? "Une erreur est survenue. Réessaie.");
  }
  return corps.data as T;
}

export async function chargerSeries(): Promise<string[]> {
  return lire<string[]>(await fetch(`${BASE}/series`));
}

export async function chargerMatieres(serie: string): Promise<MatiereSerie[]> {
  return lire<MatiereSerie[]>(await fetch(`${BASE}/series/${encodeURIComponent(serie)}/matieres`));
}

export async function recommander(
  serie: string,
  notes: NoteSaisie[],
): Promise<RecommandationResponse> {
  const reponse = await fetch(`${BASE}/recommandations`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ serie, notes }),
  });
  return lire<RecommandationResponse>(reponse);
}
