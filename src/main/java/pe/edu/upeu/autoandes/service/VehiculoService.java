package pe.edu.upeu.autoandes.service;

import java.util.List;
import pe.edu.upeu.autoandes.dto.*;
public interface VehiculoService {
 List<VehiculoResponseDTO> listar(Long clienteId);
 VehiculoResponseDTO obtener(Long id);
 VehiculoResponseDTO crear(VehiculoRequestDTO dto);
 VehiculoResponseDTO actualizar(Long id,VehiculoRequestDTO dto);
 void eliminar(Long id);
 
}
