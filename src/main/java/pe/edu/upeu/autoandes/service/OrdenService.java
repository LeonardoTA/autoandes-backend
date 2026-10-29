package pe.edu.upeu.autoandes.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.autoandes.dto.*;
public interface OrdenService {
 OrdenResponseDTO registrar(OrdenRequestDTO dto,String username);
 OrdenResponseDTO obtener(Long id);
 List<OrdenResponseDTO> listar(String placa,String estado,LocalDate desde,LocalDate hasta);
 OrdenResponseDTO anular(Long id,String motivo);
}
