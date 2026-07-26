package bj.orientation.web;

import bj.orientation.model.LigneReleve;
import bj.orientation.ocr.OcrEngine;
import bj.orientation.ocr.ReleveParser;
import bj.orientation.web.dto.ApiResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Extraction OCR d'un relevé. Le fichier est traité en mémoire et jamais conservé. */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class ReleveController {
    private final OcrEngine ocrEngine;
    private final ReleveParser parser;

    public ReleveController(OcrEngine ocrEngine, ReleveParser parser) {
        this.ocrEngine = ocrEngine;
        this.parser = parser;
    }

    @PostMapping("/releves")
    public ApiResponse<List<LigneReleve>> extraire(@RequestParam("fichier") MultipartFile fichier)
            throws IOException {
        String texte = ocrEngine.extraireTexte(fichier.getBytes(), fichier.getOriginalFilename());
        return new ApiResponse<>(parser.parser(texte));
    }
}
