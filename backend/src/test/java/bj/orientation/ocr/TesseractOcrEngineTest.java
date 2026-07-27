package bj.orientation.ocr;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TesseractOcrEngineTest {
  private final TesseractOcrEngine engine = new TesseractOcrEngine("", "fra");

  @Test
  void contenuInvalideLeveOcrIndisponible() {
    assertThatThrownBy(() -> engine.extraireTexte(new byte[] {1, 2, 3}, "x.png"))
        .isInstanceOf(OcrIndisponibleException.class);
  }
}
