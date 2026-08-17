package bj.orientation.metrics;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatRepository extends JpaRepository<StatJournaliere, Long> {
  Optional<StatJournaliere> findByJourAndSerie(LocalDate jour, String serie);
}
