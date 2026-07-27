import type { RangNote } from "../types";

interface Props {
  lignes: RangNote[];
  onChange: (lignes: RangNote[]) => void;
}

/** Tableau éditable des notes (matière, note /20, coefficient), avec ajout/suppression. */
export function NotesTable({ lignes, onChange }: Props) {
  function modifier(index: number, champ: keyof RangNote, valeur: string) {
    const copie = lignes.map((l, i) => (i === index ? { ...l, [champ]: valeur } : l));
    onChange(copie);
  }
  function supprimer(index: number) {
    onChange(lignes.filter((_, i) => i !== index));
  }
  function ajouter() {
    onChange([...lignes, { libelle: "", note: "", coefficient: "" }]);
  }

  return (
    <div>
      <table className="notes">
        <thead>
          <tr>
            <th>Matière</th>
            <th className="col-note">Note /20</th>
            <th className="col-coef">Coef.</th>
            <th className="col-act">
              <span className="sr-only">Supprimer</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {lignes.map((ligne, index) => (
            <tr key={index}>
              <td>
                <input
                  type="text"
                  aria-label={`Matière ligne ${index + 1}`}
                  placeholder="ex. Mathématiques"
                  value={ligne.libelle}
                  onChange={(e) => modifier(index, "libelle", e.target.value)}
                />
              </td>
              <td>
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
              </td>
              <td>
                <input
                  type="number"
                  inputMode="numeric"
                  min={1}
                  step="1"
                  aria-label={`Coefficient ligne ${index + 1}`}
                  value={ligne.coefficient}
                  onChange={(e) => modifier(index, "coefficient", e.target.value)}
                />
              </td>
              <td>
                <button
                  type="button"
                  className="icon-btn"
                  aria-label={`Supprimer la ligne ${index + 1}`}
                  onClick={() => supprimer(index)}
                >
                  ×
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="actions">
        <button type="button" className="btn btn--ghost" onClick={ajouter}>
          + Ajouter une matière
        </button>
      </div>
    </div>
  );
}
