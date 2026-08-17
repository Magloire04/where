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
class AdminControllerTest {
  @Autowired MockMvc mvc;

  @Test
  void statsRefuseesSansJeton() throws Exception {
    mvc.perform(get("/api/v1/admin/stats")).andExpect(status().isUnauthorized());
  }

  @Test
  void statsAvecJetonRetournentLesKpi() throws Exception {
    mvc.perform(get("/api/v1/admin/stats").header("X-Admin-Token", "dev-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalCalculs").exists())
        .andExpect(jsonPath("$.data.parJour").isArray())
        .andExpect(jsonPath("$.data.parSerie").isArray());
  }
}
