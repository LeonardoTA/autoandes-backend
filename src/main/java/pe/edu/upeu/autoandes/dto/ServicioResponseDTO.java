package pe.edu.upeu.autoandes.dto;

public record ServicioResponseDTO(Long id, String codigo, String nombre, java.math.BigDecimal precio,
                                  Integer duracionMin, Boolean activo) {
}
