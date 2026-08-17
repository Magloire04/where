package bj.orientation.metrics;

import jakarta.annotation.PreDestroy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Collecte des métriques d'usage/performance. Le chemin requête n'incrémente qu'un accumulateur en
 * mémoire (rapide) ; un flush périodique écrit les deltas en base par lots (best-effort).
 */
@Service
public class MetriquesService {
  private final StatRepository repo;
  private final Map<String, Delta> pending = new ConcurrentHashMap<>();

  public MetriquesService(StatRepository repo) {
    this.repo = repo;
  }

  /** Un calcul de recommandation : latence et éventuelle erreur, par série. */
  public void enregistrerCalcul(String serie, long latenceMs, boolean erreur) {
    String cle = (serie == null || serie.isBlank()) ? "?" : serie.trim().toUpperCase();
    Delta d = pending.computeIfAbsent(cle(LocalDate.now(), cle), k -> new Delta());
    synchronized (d) {
      d.calculs++;
      if (erreur) {
        d.erreurs++;
      }
      d.latenceTotaleMs += latenceMs;
      d.latenceCount++;
      d.latenceMaxMs = Math.max(d.latenceMaxMs, latenceMs);
    }
  }

  /** Une visite (chargement de l'application). */
  public void enregistrerVisite() {
    Delta d =
        pending.computeIfAbsent(cle(LocalDate.now(), StatJournaliere.SERIE_VISITE), k -> new Delta());
    synchronized (d) {
      d.calculs++;
    }
  }

  @Scheduled(fixedDelay = 30000)
  public void flush() {
    for (String key : new ArrayList<>(pending.keySet())) {
      Delta d = pending.remove(key);
      if (d == null) {
        continue;
      }
      try {
        appliquer(key, d);
      } catch (RuntimeException e) {
        // Best-effort : on ne perd pas le delta, on le réintègre pour le prochain flush.
        pending.merge(key, d, Delta::fusionner);
      }
    }
  }

  @PreDestroy
  public void flushAuShutdown() {
    flush();
  }

  private void appliquer(String key, Delta d) {
    String[] parts = key.split("\\|", 2);
    LocalDate jour = LocalDate.parse(parts[0]);
    String serie = parts[1];
    StatJournaliere row =
        repo.findByJourAndSerie(jour, serie).orElseGet(() -> new StatJournaliere(jour, serie));
    row.ajouter(d.calculs, d.erreurs, d.latenceTotaleMs, d.latenceCount, d.latenceMaxMs);
    repo.save(row);
  }

  private static String cle(LocalDate jour, String serie) {
    return jour + "|" + serie;
  }

  static final class Delta {
    long calculs;
    long erreurs;
    long latenceTotaleMs;
    long latenceCount;
    long latenceMaxMs;

    Delta fusionner(Delta o) {
      this.calculs += o.calculs;
      this.erreurs += o.erreurs;
      this.latenceTotaleMs += o.latenceTotaleMs;
      this.latenceCount += o.latenceCount;
      this.latenceMaxMs = Math.max(this.latenceMaxMs, o.latenceMaxMs);
      return this;
    }
  }
}
