/** Pied de page commun : note sur les données + liens légaux. */
export function Footer() {
  return (
    <footer className="footer">
      <div className="footer__inner">
        <p className="footer__note">
          Données : Guide d'orientation MESRS 2026-2027 (224 filières publiques). Aucune donnée
          personnelle n'est conservée : tes notes sont traitées le temps du calcul, puis oubliées.
        </p>
        <nav className="footer__nav" aria-label="Informations légales">
          <a href="#/confidentialite">Politique de confidentialité</a>
          <a href="#/cgu">Conditions d'utilisation</a>
          <a href="#/mentions-legales">Mentions légales</a>
        </nav>
      </div>
    </footer>
  );
}
