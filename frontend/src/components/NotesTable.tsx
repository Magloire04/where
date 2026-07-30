import type { RangNote } from "../types";

interface Props {
  lignes: RangNote[];
  onChange: (lignes: RangNote[]) => void;
}

/**
 * Notes en cartes-lignes sur mobile (matière au-dessus, note + coef + suppression en dessous)
 * qui se réalignent en une seule ligne sur écran large.
 */
export function NotesTable({ lignes, onChange }: Props) {
  function modifier(index: number, champ: keyof RangNote, valeur: string) {
    onChange(lignes.map((l, i) => (i === index ? { ...l, [champ]: valeur } : l)));
  }
  function supprimer(index: number) {
    onChange(lignes.filter((_, i) => i !== index));
  }
  function ajouter() {
    onChange([...lignes, { libelle: "", note: "", coefficient: "" }]);
  }

  return (
    <div>
      <div className="notes">
        {lignes.map((ligne, index) => (
          <div className="note-row" key={index}>
            <div className="note-row__matiere">
              <input
                type="text"
                aria-label={`Matière ligne ${index + 1}`}
                placeholder="Matière"
                value={ligne.libelle}
                onChange={(e) => modifier(index, "libelle", e.target.value)}
              />
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
      <button type="button" className="btn btn--ghost btn--block notes-ajout" onClick={ajouter}>
        + Ajouter une matière
      </button>
    </div>
  );
}
