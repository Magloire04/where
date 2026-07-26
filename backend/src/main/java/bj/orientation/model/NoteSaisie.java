package bj.orientation.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * Une note saisie par le candidat pour une matière de son relevé.
 * La note est bornée à l'intervalle [0, 20] (barème du baccalauréat béninois).
 */
public record NoteSaisie(
        String libelle,
        @DecimalMin("0") @DecimalMax("20") double note,
        @DecimalMin("0") double coefficient) {
}
