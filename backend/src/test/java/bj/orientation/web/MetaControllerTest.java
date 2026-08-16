package bj.orientation.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MetaControllerTest {
  @Autowired MockMvc mvc;

  @Test
  void matieresDeLaSerieDPreRempliesAvecCoefficients() throws Exception {
    mvc.perform(get("/api/v1/series/D/matieres"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].code").value("SVT"))
        .andExpect(jsonPath("$.data[0].libelle").value("SVT"))
        .andExpect(jsonPath("$.data[0].coefficient").value(5));
  }

  @Test
  void seriesLimiteesAEnseignementGeneral() throws Exception {
    mvc.perform(get("/api/v1/series"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(5))
        .andExpect(jsonPath("$.data[0]").value("A1"))
        .andExpect(jsonPath("$.data[4]").value("D"));
  }

  @Test
  void serieInconnueRenvoie400() throws Exception {
    mvc.perform(get("/api/v1/series/ZZZ/matieres"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("SERIE_INVALIDE"));
  }
}
