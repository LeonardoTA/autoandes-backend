package pe.edu.upeu.autoandes.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.autoandes.dto.IngresoServicioDTO;
public interface ReporteService {List<IngresoServicioDTO> ingresosPorServicio(LocalDate desde,LocalDate hasta);}
