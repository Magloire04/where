package bj.orientation.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** Vérifie qu'un fichier trop volumineux renvoie un 413 propre (et non une connexion coupée). */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.servlet.multipart.max-file-size=1KB",
      "spring.servlet.multipart.max-request-size=2KB"
    })
class UploadTailleTest {
  @Autowired TestRestTemplate rest;

  @Test
  void fichierTropGrosRetourne413() {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "fichier",
        new ByteArrayResource(new byte[4096]) {
          @Override
          public String getFilename() {
            return "releve.png";
          }
        });
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    ResponseEntity<String> reponse =
        rest.postForEntity("/api/v1/releves", new HttpEntity<>(body, headers), String.class);

    assertThat(reponse.getStatusCode().value()).isEqualTo(413);
    assertThat(reponse.getBody()).contains("FICHIER_TROP_GROS");
  }
}
