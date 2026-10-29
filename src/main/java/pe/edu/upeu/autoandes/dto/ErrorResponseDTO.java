package pe.edu.upeu.autoandes.dto;

import java.time.LocalDateTime;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(LocalDateTime timestamp,int status,String error,String mensaje,String ruta,Map<String,String> campos) {}
