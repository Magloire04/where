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
  /** Codes des matières (parmi les 3 déclarées) réellement utilisées pour le calcul. */
  matieresRetenues: string[];
}

export interface RecommandationResponse {
  /** Filières calculées sur leur triplet complet, triées par chance d'allocation. */
  recommandations: Recommandation[];
  /** Codes des matières manquantes à saisir pour couvrir des filières « 2 sur 3 ». */
  matieresACompleter: string[];
  concours: Filiere[];
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
