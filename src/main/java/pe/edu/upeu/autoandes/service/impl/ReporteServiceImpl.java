package pe.edu.upeu.autoandes.service.impl;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.service.*;
import pe.edu.upeu.autoandes.repository.DetalleOrdenRepository;
import pe.edu.upeu.autoandes.dto.IngresoServicioDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {
    private final DetalleOrdenRepository detalles;

    public List<IngresoServicioDTO> ingresosPorServicio(LocalDate desde, LocalDate hasta) {
        var rango = RangoFechas.de(desde, hasta);
        return detalles.ingresosPorServicio(rango.desde(), rango.hasta());
    }
}
