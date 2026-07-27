import { describe, expect, it } from "vitest";
import { classeStatut, libelleStatut, pourcentage } from "./statut";

describe("libelleStatut", () => {
  it("traduit chaque statut en libellé lisible", () => {
    expect(libelleStatut("BOURSIER")).toBe("Boursier");
    expect(libelleStatut("AIDE")).toBe("Aide / demi-bourse");
    expect(libelleStatut("PAYANT")).toBe("Payant");
    expect(libelleStatut("PAYANT_UNIQUEMENT")).toBe("Payant");
    expect(libelleStatut("CONCOURS")).toBe("Concours");
  });
});

describe("classeStatut", () => {
  it("mappe vers la classe de couleur", () => {
    expect(classeStatut("BOURSIER")).toBe("boursier");
    expect(classeStatut("AIDE")).toBe("aide");
    expect(classeStatut("PAYANT")).toBe("payant");
    expect(classeStatut("PAYANT_UNIQUEMENT")).toBe("payant");
  });
});

describe("pourcentage", () => {
  it("formate une fraction en pourcentage arrondi", () => {
    expect(pourcentage(0.82)).toBe("82 %");
    expect(pourcentage(0)).toBe("0 %");
    expect(pourcentage(1)).toBe("100 %");
    expect(pourcentage(0.125)).toBe("13 %");
  });
});
