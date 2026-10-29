package pe.edu.upeu.autoandes.config;

import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.autoandes.security.*;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {
    private final JwtService jwt;
    private final UsuarioDetailsService usuarios;
    private final RestAuthenticationEntryPoint entryPoint;
    private final RestAccessDeniedHandler denied;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            @org.springframework.beans.factory.annotation.Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {
        return http.csrf(c -> c.disable()).cors(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(c -> c.disable()).formLogin(c -> c.disable()).httpBasic(c -> c.disable()).logout(c -> c.disable())
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(denied))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/servicios", "/api/v1/servicios/**").hasAuthority("SERVICIO_LEER")
                        .requestMatchers("/api/v1/servicios", "/api/v1/servicios/**").hasAuthority("SERVICIO_ESCRIBIR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes", "/api/v1/vehiculos", "/api/v1/vehiculos/**").hasAuthority("VEHICULO_LEER")
                        .requestMatchers("/api/v1/vehiculos", "/api/v1/vehiculos/**").hasAuthority("VEHICULO_ESCRIBIR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes", "/api/v1/ordenes/**").hasAuthority("ORDEN_LEER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/ordenes").hasAuthority("ORDEN_REGISTRAR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/ordenes/*/anular").hasAuthority("ORDEN_ANULAR")
                        .requestMatchers("/api/v1/reportes/**").hasAuthority("REPORTE_VER")
                        .anyRequest().authenticated())
                .addFilterAt(new RestCorsFilter(cors, denied), org.springframework.web.filter.CorsFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwt, usuarios, entryPoint), UsernamePasswordAuthenticationFilter.class).build();
    }
}

