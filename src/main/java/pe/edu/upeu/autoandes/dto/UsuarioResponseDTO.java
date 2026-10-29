package pe.edu.upeu.autoandes.dto;

public record UsuarioResponseDTO(Long id,String username,String nombreCompleto,java.util.List<String> roles,java.util.List<String> permisos) {}
