/** Page d'aide : comment utiliser le site, en étapes. */
export function Aide() {
  return (
    <article className="legal">
      <h1>Comment utiliser le site</h1>
      <p className="legal__maj">
        « Après mon bac » estime, à titre indicatif, tes chances de bourse ou d'aide par filière, à
        partir de tes meilleures notes.
      </p>

      <h2>1. Choisis ta série</h2>
      <p>
        Sélectionne ta série de l'enseignement général (A1, A2, B, C ou D). Les matières de la série
        deviennent disponibles.
      </p>

      <h2>2. Renseigne tes 3 matières les plus fortes</h2>
      <p>
        Choisis les 3 matières où tu as tes meilleures notes, puis saisis pour chacune ta note sur
        20 et son coefficient (déjà pré-rempli pour les séries C et D).
      </p>

      <h2>3. Clique sur « Voir mes chances »</h2>
      <p>
        On liste les filières qui calculent leur classement sur ces matières, chacune avec ton
        statut estimé (boursier ou aidé) et ton pourcentage de chance.
      </p>

      <h2>4. Affine si on te le demande</h2>
      <p>
        Certaines filières se calculent aussi sur une matière que tu n'as pas encore saisie. Si la
        section « Affine ton estimation » apparaît, renseigne les matières demandées puis clique sur
        « Recalculer » : ces filières apparaissent alors, calculées sur leurs 3 matières exactes.
      </p>

      <h2>5. Lis tes résultats</h2>
      <p>
        Les filières sont classées de la meilleure à la plus faible chance : d'abord un top 3
        global, puis par catégorie d'établissement (Facultés, Écoles, Instituts, Centres).
      </p>

      <h2>Bon à savoir</h2>
      <p>
        Les résultats sont une estimation indicative : ils ne garantissent pas ton classement par
        l'État. Seuls les résultats officiels du MESRS font foi. Aucune donnée n'est conservée.
      </p>
    </article>
  );
}
