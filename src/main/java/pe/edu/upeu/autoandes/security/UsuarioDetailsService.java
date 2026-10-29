package pe.edu.upeu.autoandes.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.*;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;

    @Override
    @Transactional(readOnly = true)
    public UsuarioPrincipal loadUserByUsername(String username) {
        var u = usuarios.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
        var roles = u.getRoles().stream().map(r -> r.getNombre()).sorted().toList();
        return new UsuarioPrincipal(u.getId(), u.getUsername(), u.getPassword(), u.getNombreCompleto(), Boolean.TRUE.equals(u.getActivo()), roles, PermisosPorRol.de(roles));
    }
}
