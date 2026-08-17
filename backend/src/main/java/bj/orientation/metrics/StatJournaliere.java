package bj.orientation.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

/**
 * Compteurs agrégés et anonymes d'un jour pour une série. Ne contient aucune donnée personnelle
 * (ni note, ni identifiant) : uniquement des totaux d'usage et de performance.
 */
@Entity
@Table(
    name = "stat_journaliere",
    uniqueConstraints = @UniqueConstraint(columnNames = {"jour", "serie"}))
public class StatJournaliere {

  /** Clé de série réservée au comptage des visites (chargement de la page). */
  public static final String SERIE_VISITE = "_VISITE_";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private LocalDate jour;

  @Column(nullable = false)
  private String serie;

  private long calculs;
  private long erreurs;
  private long latenceTotaleMs;
  private long latenceCount;
  private long latenceMaxMs;

  protected StatJournaliere() {}

  public StatJournaliere(LocalDate jour, String serie) {
    this.jour = jour;
    this.serie = serie;
  }

  /** Ajoute des deltas accumulés en mémoire à la ligne persistée. */
  public void ajouter(
      long calculs, long erreurs, long latenceTotaleMs, long latenceCount, long latenceMaxMs) {
    this.calculs += calculs;
    this.erreurs += erreurs;
    this.latenceTotaleMs += latenceTotaleMs;
    this.latenceCount += latenceCount;
    this.latenceMaxMs = Math.max(this.latenceMaxMs, latenceMaxMs);
  }

  public LocalDate getJour() {
    return jour;
  }

  public String getSerie() {
    return serie;
  }

  public long getCalculs() {
    return calculs;
  }

  public long getErreurs() {
    return erreurs;
  }

  public long getLatenceTotaleMs() {
    return latenceTotaleMs;
  }

  public long getLatenceCount() {
    return latenceCount;
  }

  public long getLatenceMaxMs() {
    return latenceMaxMs;
  }
}
