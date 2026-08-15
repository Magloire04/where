export type CategorieEtab = "Faculté" | "École" | "Institut" | "Centre" | "Autre";

/** Ordre d'affichage des sections : des facultés vers les écoles, puis instituts/centres/autres. */
export const ORDRE_CATEGORIES: CategorieEtab[] = [
  "Faculté",
  "École",
  "Institut",
  "Centre",
  "Autre",
];

const LIBELLES: Record<CategorieEtab, string> = {
  Faculté: "Facultés",
  École: "Écoles",
  Institut: "Instituts",
  Centre: "Centres",
  Autre: "Autres établissements",
};

/** Catégorise un établissement d'après son intitulé (préfixe, sans accents ni casse). */
export function categorieEtablissement(nom: string): CategorieEtab {
  const n = nom.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase().trim();
  if (n.startsWith("facult")) return "Faculté";
  if (n.startsWith("ecole") || n.startsWith("haute ecole")) return "École";
  if (n.startsWith("institut")) return "Institut";
  if (n.startsWith("centre")) return "Centre";
  return "Autre";
}

/** Libellé pluriel d'une catégorie pour un titre de section. */
export function libelleCategorie(cat: CategorieEtab): string {
  return LIBELLES[cat];
}
