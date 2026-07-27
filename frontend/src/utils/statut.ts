import type { StatutEstime } from "../types";

/** Libellé lisible du statut estimé (vocabulaire du bachelier). */
export function libelleStatut(statut: StatutEstime): string {
  switch (statut) {
    case "BOURSIER":
      return "Boursier";
    case "AIDE":
      return "Aide / demi-bourse";
    case "CONCOURS":
      return "Concours";
    default:
      return "Payant";
  }
}

/** Classe CSS de couleur associée au statut (boursier / aide / payant). */
export function classeStatut(statut: StatutEstime): "boursier" | "aide" | "payant" {
  if (statut === "BOURSIER") return "boursier";
  if (statut === "AIDE") return "aide";
  return "payant";
}

/** Formate une fraction [0,1] en pourcentage arrondi, ex. 0.82 -> "82 %". */
export function pourcentage(fraction: number): string {
  return `${Math.round(fraction * 100)} %`;
}
