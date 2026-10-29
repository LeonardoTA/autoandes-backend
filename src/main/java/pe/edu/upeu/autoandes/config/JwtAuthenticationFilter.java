package pe.edu.upeu.autoandes.config;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.JwtException;
import pe.edu.upeu.autoandes.security.*;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UsuarioDetailsService usuarios;
    private final RestAuthenticationEntryPoint entryPoint;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest r) {
        String path = r.getRequestURI().substring(r.getContextPath().length());
        return r.getMethod().equals("OPTIONS") || (r.getMethod().equals("POST") && path.equals("/api/v1/auth/login")) || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header == null) {
            chain.doFilter(req, res);
            return;
        }
        try {
            if (!header.startsWith("Bearer ") || header.length() <= 7)
                throw new BadCredentialsException("Token inválido");
            var usuario = usuarios.loadUserByUsername(jwt.validarYObtenerUsuario(header.substring(7)));
            if (!usuario.isEnabled()) throw new DisabledException("Cuenta inactiva");
            var auth = UsernamePasswordAuthenticationToken.authenticated(usuario, null, usuario.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            var contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(auth);
            SecurityContextHolder.setContext(contexto);
        } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(req, res, new BadCredentialsException("Token inválido"));
            return;
        }
        chain.doFilter(req, res);
    }
}
