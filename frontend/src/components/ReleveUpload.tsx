import { useRef, useState } from "react";
import { extraireReleve } from "../api/client";
import type { LigneReleve, RangNote } from "../types";

interface Props {
  onExtrait: (lignes: RangNote[]) => void;
}

/** Téléversement du relevé : OCR côté serveur → lignes pré-remplies (à corriger). */
export function ReleveUpload({ onExtrait }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  async function traiter(fichier: File) {
    setChargement(true);
    setErreur(null);
    try {
      const lignes = await extraireReleve(fichier);
      if (lignes.length === 0) {
        setErreur("Aucune note détectée. Saisis-les à la main ci-dessous.");
        return;
      }
      onExtrait(lignes.map(versRangNote));
    } catch (e) {
      setErreur(e instanceof Error ? e.message : "OCR indisponible. Saisis tes notes à la main.");
    } finally {
      setChargement(false);
    }
  }

  return (
    <div>
      <div className="dropzone">
        <p>
          <strong>Téléverse ton relevé</strong> (image ou PDF).
        </p>
        <p className="hint">
          On lit les notes automatiquement — tu pourras tout corriger avant de calculer.
        </p>
        <div className="actions" style={{ justifyContent: "center" }}>
          <button
            type="button"
            className="btn btn--ghost"
            disabled={chargement}
            onClick={() => inputRef.current?.click()}
          >
            {chargement ? "Lecture…" : "Choisir un fichier"}
          </button>
        </div>
        <input
          ref={inputRef}
          type="file"
          accept="image/*,application/pdf"
          hidden
          onChange={(e) => {
            const fichier = e.target.files?.[0];
            if (fichier) traiter(fichier);
          }}
        />
      </div>
      {erreur && (
        <div className="alert alert--error" role="alert">
          {erreur}
        </div>
      )}
    </div>
  );
}

function versRangNote(ligne: LigneReleve): RangNote {
  return {
    libelle: ligne.libelle,
    note: ligne.note == null ? "" : String(ligne.note),
    coefficient: ligne.coefficient == null ? "" : String(ligne.coefficient),
  };
}
