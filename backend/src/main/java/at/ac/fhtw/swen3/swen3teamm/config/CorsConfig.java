package at.ac.fhtw.swen3.swen3teamm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:8080", "http://localhost:5173") //für vite
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}

// Unnötig weil:
// Weil Nginx UI und API unter derselben Domain/Port ausliefert und der Browser dadurch keinen
// Cross-Origin-Check mehr macht, ist eine separate CORS-Konfiguration überflüssig.
// Weil es von der selben Origin kommt.

//--> auch somit Test Fett unnötig