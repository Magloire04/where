package bj.orientation.config;

import bj.orientation.model.Palier;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/** Paramètres externalisés de l'estimateur (seuils par palier, étalement, liste prestige). */
@ConfigurationProperties(prefix = "estimateur")
public record EstimateurProperties(double sigma, Map<Palier, Seuils> paliers, List<String> prestige) {

    /** Seuils estimés (sur /20) d'un palier. {@code aide} peut être null (palier sans coussin). */
    public record Seuils(double bourse, Double aide) {
    }
}
