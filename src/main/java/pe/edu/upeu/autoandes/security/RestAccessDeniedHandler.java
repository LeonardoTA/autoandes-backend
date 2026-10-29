package pe.edu.upeu.autoandes.security;

import java.io.IOException;
import java.time.*;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;
import pe.edu.upeu.autoandes.dto.ErrorResponseDTO;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final JsonMapper json;
    private final Clock clock;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        log.warn("Acceso rechazado: estado=403 ruta={}", req.getRequestURI());
        res.setStatus(403);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getOutputStream(), new ErrorResponseDTO(LocalDateTime.now(clock), 403, "Forbidden", "No tiene permiso para realizar esta operación", req.getRequestURI(), null));
    }
}
