package bj.orientation.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RecommandationControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void recommanderRetourneDesRecommandationsPourUnEleveD() throws Exception {
        String body = """
            {"serie":"D","matieresFortes":["Maths","PCT","SVT"],"notes":[
              {"libelle":"Maths","note":16,"coefficient":4},
              {"libelle":"PCT","note":15,"coefficient":4},
              {"libelle":"SVT","note":17,"coefficient":5}
            ]}""";
        mvc.perform(post("/api/v1/recommandations").contentType("application/json").content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data.recommandations").isArray())
           .andExpect(jsonPath("$.data.recommandations[0].filiere.filiere").exists())
           .andExpect(jsonPath("$.data.recommandations[0].matieresRetenues").isArray())
           .andExpect(jsonPath("$.data.matieresACompleter").isArray());
    }

    @Test
    void requeteInvalideRetourne400AvecEnveloppeErreur() throws Exception {
        String body = "{\"serie\":\"\",\"notes\":[],\"matieresFortes\":[]}";
        mvc.perform(post("/api/v1/recommandations").contentType("application/json").content(body))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.error.code").exists());
    }

    @Test
    void noteHorsBaremeRetourne400() throws Exception {
        String body = """
            {"serie":"D","matieresFortes":["Maths"],"notes":[{"libelle":"Maths","note":25,"coefficient":4}]}""";
        mvc.perform(post("/api/v1/recommandations").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }

    @Test
    void seriesRetourneLaListeSousData() throws Exception {
        mvc.perform(get("/api/v1/series"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data[0]").exists());
    }
}
