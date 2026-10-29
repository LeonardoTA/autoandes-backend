package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.security.UsuarioPrincipal;

@Component
public class UsuarioMapper {
    public UsuarioResponseDTO respuesta(UsuarioPrincipal u) {
        return new UsuarioResponseDTO(u.id(), u.username(), u.nombreCompleto(), u.roles(), u.permisos());
    }

    public LoginResponseDTO login(UsuarioPrincipal u, String token, long segundos) {
        return new LoginResponseDTO(token, "Bearer", segundos, u.nombreCompleto(), u.username(), u.roles(), u.permisos());
    }
}
