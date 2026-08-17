package bj.orientation.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Applique le CORS (origines restreintes, configurables) et protège l'espace de monitoring. La
 * racine {@code /} sert l'application (frontend statique embarqué) ; la doc reste sur {@code /docs.html}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final List<String> allowedOrigins;
  private final String adminToken;

  public WebConfig(
      @Value("${app.cors.allowed-origins}") List<String> allowedOrigins,
      @Value("${admin.token}") String adminToken) {
    this.allowedOrigins = allowedOrigins;
    this.adminToken = adminToken;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new AdminAuthInterceptor(adminToken)).addPathPatterns("/api/v1/admin/**");
  }

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}
