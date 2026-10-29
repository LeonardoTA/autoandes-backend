package pe.edu.upeu.autoandes.service;

import java.util.List;
import pe.edu.upeu.autoandes.dto.*;
public interface ServicioService {
 List<ServicioResponseDTO> listar();
 ServicioResponseDTO obtener(Long id);
 ServicioResponseDTO crear(ServicioRequestDTO dto);
 ServicioResponseDTO actualizar(Long id,ServicioRequestDTO dto);
 void eliminar(Long id);
 ServicioResponseDTO estado(Long id,boolean activo);
}
