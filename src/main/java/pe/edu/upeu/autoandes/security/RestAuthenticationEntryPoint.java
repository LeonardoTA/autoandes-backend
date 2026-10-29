package pe.edu.upeu.autoandes.security;

import java.io.IOException;
import java.time.*;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.json.JsonMapper;
import pe.edu.upeu.autoandes.dto.ErrorResponseDTO;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final JsonMapper json;
    private final Clock clock;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        log.warn("Acceso rechazado: estado=401 ruta={}", req.getRequestURI());
        res.setStatus(401);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getOutputStream(), new ErrorResponseDTO(LocalDateTime.now(clock), 401, "Unauthorized", "Se requiere autenticación válida", req.getRequestURI(), null));
    }
}
