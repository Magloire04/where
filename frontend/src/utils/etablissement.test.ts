import { describe, expect, it } from "vitest";
import { categorieEtablissement, libelleCategorie, ORDRE_CATEGORIES } from "./etablissement";

describe("categorieEtablissement", () => {
  it("classe par préfixe d'intitulé", () => {
    expect(categorieEtablissement("Faculté des Sciences de la Santé (FSS)")).toBe("Faculté");
    expect(categorieEtablissement("Ecole Polytechnique d'Abomey-Calavi (EPAC)")).toBe("École");
    expect(categorieEtablissement("Haute Ecole Régionale de Commerce International (HERCI)")).toBe(
      "École",
    );
    expect(categorieEtablissement("Institut National de l'Eau (INE)")).toBe("Institut");
    expect(
      categorieEtablissement("Centre de Formation et de Recherche en matière de Population"),
    ).toBe("Centre");
    expect(categorieEtablissement("UFR Gestion du Développement (UFR/GD)")).toBe("Autre");
  });

  it("ordonne des facultés vers les écoles, puis instituts/centres/autres", () => {
    expect(ORDRE_CATEGORIES).toEqual(["Faculté", "École", "Institut", "Centre", "Autre"]);
  });

  it("donne un libellé pluriel de section", () => {
    expect(libelleCategorie("Faculté")).toBe("Facultés");
    expect(libelleCategorie("École")).toBe("Écoles");
  });
});
