package bj.orientation.web;

import bj.orientation.metrics.StatJournaliere;
import bj.orientation.metrics.StatRepository;
import bj.orientation.web.dto.ApiResponse;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Espace de monitoring : KPIs agrégés. Protégé par jeton (voir AdminAuthInterceptor). */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
  private final StatRepository repo;

  public AdminController(StatRepository repo) {
    this.repo = repo;
  }

  @GetMapping("/stats")
  public ApiResponse<StatsResponse> stats() {
    long totalCalculs = 0;
    long totalVisites = 0;
    long totalErreurs = 0;
    long latenceTotale = 0;
    long latenceCount = 0;
    long latenceMax = 0;
    Map<String, Long> parJour = new TreeMap<>();
    Map<String, Long> parSerie = new TreeMap<>();

    for (StatJournaliere s : repo.findAll()) {
      if (StatJournaliere.SERIE_VISITE.equals(s.getSerie())) {
        totalVisites += s.getCalculs();
        continue;
      }
      totalCalculs += s.getCalculs();
      totalErreurs += s.getErreurs();
      latenceTotale += s.getLatenceTotaleMs();
      latenceCount += s.getLatenceCount();
      latenceMax = Math.max(latenceMax, s.getLatenceMaxMs());
      parJour.merge(s.getJour().toString(), s.getCalculs(), Long::sum);
      parSerie.merge(s.getSerie(), s.getCalculs(), Long::sum);
    }

    long latenceMoyenne = latenceCount > 0 ? Math.round((double) latenceTotale / latenceCount) : 0;
    double tauxErreur = totalCalculs > 0 ? (double) totalErreurs / totalCalculs : 0;
    String demarrage =
        Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()).toString();

    return new ApiResponse<>(
        new StatsResponse(
            totalCalculs,
            totalVisites,
            latenceMoyenne,
            latenceMax,
            tauxErreur,
            demarrage,
            points(parJour),
            points(parSerie)));
  }

  private static List<Point> points(Map<String, Long> map) {
    return map.entrySet().stream().map(e -> new Point(e.getKey(), e.getValue())).toList();
  }

  public record Point(String cle, long valeur) {}

  public record StatsResponse(
      long totalCalculs,
      long totalVisites,
      long latenceMoyenneMs,
      long latenceMaxMs,
      double tauxErreur,
      String demarrage,
      List<Point> parJour,
      List<Point> parSerie) {}
}
