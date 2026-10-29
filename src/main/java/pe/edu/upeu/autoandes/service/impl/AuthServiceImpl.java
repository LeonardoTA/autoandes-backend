package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.autoandes.config.JwtService;
import pe.edu.upeu.autoandes.security.*;
import pe.edu.upeu.autoandes.service.AuthService;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.mapper.UsuarioMapper;
@Service @RequiredArgsConstructor @Slf4j
public class AuthServiceImpl implements AuthService {
 private final UsuarioDetailsService usuarios;private final PasswordEncoder encoder;private final JwtService jwt;private final UsuarioMapper mapper;
 // A valid work factor keeps nonexistent-user login on the same BCrypt comparison path.
 private static final String DUMMY_HASH="$2a$10$AM38cMk/l7xtopPz5bj3vejZxruZw8.9CT/5WFc6/BK7g9v8nF6Om";
 public LoginResponseDTO login(LoginRequestDTO d) {
  UsuarioPrincipal u;
  try {u=usuarios.loadUserByUsername(d.username());}
  catch(UsernameNotFoundException ex) {encoder.matches(d.password(),DUMMY_HASH);throw new BadCredentialsException("Credenciales inválidas");}
  boolean valido=encoder.matches(d.password(),u.getPassword());
  if(!valido || !u.isEnabled()) throw new BadCredentialsException("Credenciales inválidas");
  log.info("Inicio de sesión exitoso");return mapper.login(u,jwt.generar(u.getUsername()),jwt.expiraEnSegundos());
 }
 public UsuarioResponseDTO actual(String username) {return mapper.respuesta(usuarios.loadUserByUsername(username));}
}
