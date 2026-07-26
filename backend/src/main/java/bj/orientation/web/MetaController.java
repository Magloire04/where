package bj.orientation.web;

import bj.orientation.model.Serie;
import bj.orientation.web.dto.ApiResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Métadonnées de référence (séries de bac). */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class MetaController {

    @GetMapping("/series")
    public ApiResponse<List<String>> series() {
        return new ApiResponse<>(Arrays.stream(Serie.values()).map(Enum::name).toList());
    }
}
