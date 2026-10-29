package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.*;
public interface AuthService {
 LoginResponseDTO login(LoginRequestDTO dto);
 UsuarioResponseDTO actual(String username);
}
