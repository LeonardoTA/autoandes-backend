package pe.edu.upeu.autoandes.dto;

public record LoginResponseDTO(String token, String tipo, long expiraEnSegundos, String nombreCompleto, String username,
                               java.util.List<String> roles, java.util.List<String> permisos) {
}
