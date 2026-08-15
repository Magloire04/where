/** Libellé lisible d'un code matière canonique renvoyé par le backend. */
const LIBELLES: Record<string, string> = {
  MATHS: "Maths",
  PCT: "PCT",
  SVT: "SVT",
  FR: "Français",
  PHILO: "Philo",
  HG: "Hist-Géo",
  ANG: "Anglais",
  ANG2: "Anglais (LV2)",
  ESP: "Espagnol",
  ALL: "Allemand",
  ECO: "Économie",
  EDC: "Étude de cas",
  CG: "Culture générale",
};

export function libelleMatiere(code: string): string {
  return LIBELLES[code] ?? code;
}
