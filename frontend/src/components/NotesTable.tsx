import type { MatiereSerie, RangNote } from "../types";

interface Props {
  lignes: RangNote[];
  onChange: (lignes: RangNote[]) => void;
  /** Si fourni : la matière se choisit dans la liste de la série et le coefficient est pré-rempli. */
  options?: MatiereSerie[];
  /** Nombre maximum de lignes (ex. 3 matières fortes). */
  max?: number;
}

/**
 * Saisie des matières fortes en cartes-lignes sur mobile (matière au-dessus, note + coef en dessous)
 * qui se réalignent sur une ligne en écran large. Avec `options`, la matière devient une liste
 * déroulante propre à la série et le coefficient officiel (C/D) se remplit tout seul.
 */
export function NotesTable({ lignes, onChange, options, max }: Props) {
  function modifier(index: number, champ: keyof RangNote, valeur: string) {
    onChange(lignes.map((l, i) => (i === index ? { ...l, [champ]: valeur } : l)));
  }
  function choisirMatiere(index: number, libelle: string) {
    const opt = options?.find((o) => o.libelle === libelle);
    const coefficient = opt && opt.coefficient != null ? String(opt.coefficient) : "";
    onChange(lignes.map((l, i) => (i === index ? { ...l, libelle, coefficient } : l)));
  }
  function supprimer(index: number) {
    onChange(lignes.filter((_, i) => i !== index));
  }
  function ajouter() {
    onChange([...lignes, { libelle: "", note: "", coefficient: "" }]);
  }

  const dejaChoisies = new Set(lignes.map((l) => l.libelle).filter(Boolean));
  const peutAjouter = max == null || lignes.length < max;

  return (
    <div>
      <div className="notes">
        {lignes.map((ligne, index) => (
          <div className="note-row" key={index}>
            <div className="note-row__matiere">
              {options ? (
                <select
                  aria-label={`Matière forte ${index + 1}`}
                  value={ligne.libelle}
                  onChange={(e) => choisirMatiere(index, e.target.value)}
                >
                  <option value="">Choisis une matière</option>
                  {options
                    .filter((o) => o.libelle === ligne.libelle || !dejaChoisies.has(o.libelle))
                    .map((o) => (
                      <option key={o.code} value={o.libelle}>
                        {o.libelle}
                      </option>
                    ))}
                </select>
              ) : (
                <input
                  type="text"
                  aria-label={`Matière ligne ${index + 1}`}
                  placeholder="Matière"
                  value={ligne.libelle}
                  onChange={(e) => modifier(index, "libelle", e.target.value)}
                />
              )}
            </div>
            <label className="note-cell note-cell--note">
              <span className="note-cell__lab">Note /20</span>
              <input
                type="number"
                inputMode="decimal"
                min={0}
                max={20}
                step="0.25"
                aria-label={`Note ligne ${index + 1}`}
                value={ligne.note}
                onChange={(e) => modifier(index, "note", e.target.value)}
              />
            </label>
            <label className="note-cell note-cell--coef">
              <span className="note-cell__lab">Coef.</span>
              <input
                type="number"
                inputMode="numeric"
                min={1}
                step="1"
                aria-label={`Coefficient ligne ${index + 1}`}
                value={ligne.coefficient}
                onChange={(e) => modifier(index, "coefficient", e.target.value)}
              />
            </label>
            <button
              type="button"
              className="note-row__del"
              aria-label={`Supprimer la ligne ${index + 1}`}
              onClick={() => supprimer(index)}
            >
              ×
            </button>
          </div>
        ))}
      </div>
      {peutAjouter && (
        <button type="button" className="btn btn--ghost btn--block notes-ajout" onClick={ajouter}>
          + Ajouter une matière
        </button>
      )}
    </div>
  );
}
