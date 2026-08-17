package bj.orientation.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MetriquesServiceTest {

  @Test
  void accumuleLesCalculsEtLesEcritAuFlush() {
    StatRepository repo = mock(StatRepository.class);
    when(repo.findByJourAndSerie(any(), any())).thenReturn(Optional.empty());
    MetriquesService service = new MetriquesService(repo);

    service.enregistrerCalcul("D", 100, false);
    service.enregistrerCalcul("D", 200, true);
    service.enregistrerVisite();
    service.flush();

    ArgumentCaptor<StatJournaliere> cap = ArgumentCaptor.forClass(StatJournaliere.class);
    verify(repo, atLeast(2)).save(cap.capture());

    StatJournaliere d =
        cap.getAllValues().stream()
            .filter(s -> "D".equals(s.getSerie()))
            .findFirst()
            .orElseThrow();
    assertThat(d.getCalculs()).isEqualTo(2);
    assertThat(d.getErreurs()).isEqualTo(1);
    assertThat(d.getLatenceCount()).isEqualTo(2);
    assertThat(d.getLatenceMaxMs()).isEqualTo(200);

    StatJournaliere visite =
        cap.getAllValues().stream()
            .filter(s -> StatJournaliere.SERIE_VISITE.equals(s.getSerie()))
            .findFirst()
            .orElseThrow();
    assertThat(visite.getCalculs()).isEqualTo(1);
  }
}
