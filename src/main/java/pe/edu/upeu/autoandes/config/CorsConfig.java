package pe.edu.upeu.autoandes.config;

import java.util.List;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class CorsConfig implements WebMvcConfigurer {
 @Bean CorsConfigurationSource corsConfigurationSource() {
  CorsConfiguration c=new CorsConfiguration();c.setAllowedOrigins(List.of("http://localhost:4200"));
  c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
  c.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));c.setMaxAge(3600L);
  UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/api/**",c);return source;
 }
 @Override public void addCorsMappings(CorsRegistry registry) {
  registry.addMapping("/api/**").allowedOrigins("http://localhost:4200").allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS").allowedHeaders("Authorization","Content-Type","Accept").maxAge(3600);
 }
}
