package bj.orientation.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bj.orientation.ocr.OcrEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ReleveControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    OcrEngine ocrEngine;

    @Test
    void extraitLesLignesDuReleve() throws Exception {
        when(ocrEngine.extraireTexte(any(), any())).thenReturn("Mathématiques 12,50 4");
        var fichier = new MockMultipartFile("fichier", "releve.png", "image/png", new byte[] {1, 2, 3});
        mvc.perform(multipart("/api/v1/releves").file(fichier))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].libelle").value("Mathématiques"))
            .andExpect(jsonPath("$.data[0].note").value(12.5));
    }
}
