export type StatutEstime = "BOURSIER" | "AIDE" | "PAYANT" | "CONCOURS" | "PAYANT_UNIQUEMENT";

export interface Filiere {
  num: number;
  universite: string;
  etablissement: string;
  filiere: string;
  quotaBourse: number;
  quotaAideFpp: number;
  modeEntree: string;
  seriesBacRaw: string;
  matieresRaw: string;
  debouches: string[];
  page: number;
}

export interface Probabilites {
  pBourse: number;
  pAide: number;
  pPayant: number;
  statut: StatutEstime;
  pctAffiche: number;
}

export interface Recommandation {
  filiere: Filiere;
  moyenne: number;
  proba: Probabilites;
  argumentaire: string;
}

export interface RecommandationResponse {
  top3: Recommandation[];
  alternatives: Recommandation[];
  concours: Filiere[];
  payantes: Filiere[];
  donneesInsuffisantes: Filiere[];
}

export interface NoteSaisie {
  libelle: string;
  note: number;
  coefficient: number;
}

/** Matière pré-affichée pour une série (coefficient null = à saisir depuis le relevé). */
export interface MatiereSerie {
  code: string;
  libelle: string;
  coefficient: number | null;
}

/** Ligne de note éditable dans l'interface (valeurs en texte pour la saisie). */
export interface RangNote {
  libelle: string;
  note: string;
  coefficient: string;
}
