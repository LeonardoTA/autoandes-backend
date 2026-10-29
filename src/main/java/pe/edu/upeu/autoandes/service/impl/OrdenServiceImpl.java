package pe.edu.upeu.autoandes.service.impl;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.exception.ApiException;
import pe.edu.upeu.autoandes.mapper.OrdenMapper;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.service.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrdenServiceImpl implements OrdenService {
    private final OrdenServicioRepository ordenes;
    private final VehiculoRepository vehiculos;
    private final ServicioRepository servicios;
    private final OrdenMapper mapper;
    private final Clock clock;

    public OrdenResponseDTO obtener(Long id) {
        return mapper.respuesta(ordenes.findById(id).orElseThrow(() -> ApiException.noExiste("Orden")));
    }

    public List<OrdenResponseDTO> listar(String placa, String estado, LocalDate desde, LocalDate hasta) {
        var rango = RangoFechas.de(desde, hasta);
        if (estado != null && !Set.of("REGISTRADA", "ANULADA").contains(estado))
            throw ApiException.invalido("Estado debe ser REGISTRADA o ANULADA");
        String filtro = placa == null || placa.isBlank() ? null : placa.trim();
        return ordenes.filtrar(filtro, estado, rango.desde(), rango.hasta()).stream().map(mapper::respuesta).toList();
    }

    @Transactional
    public OrdenResponseDTO registrar(OrdenRequestDTO dto, String username) {
        if (dto.detalles() == null || dto.detalles().isEmpty() || dto.detalles().size() > 10)
            throw ApiException.invalido("La orden debe tener entre 1 y 10 detalles");
        if (dto.kilometraje() == null || dto.kilometraje() < 0 || dto.kilometraje() > 9999999)
            throw ApiException.invalido("Kilometraje debe estar entre 0 y 9999999");
        // Serialize registrations for the same vehicle, preventing concurrent mileage regressions.
        Vehiculo v = vehiculos.findForUpdateById(dto.vehiculoId()).orElseThrow(() -> ApiException.noExiste("Vehículo"));
        var ultima = ordenes.findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(v.getId(), "REGISTRADA");
        if (ultima.isPresent() && dto.kilometraje() < ultima.get().getKilometraje())
            throw ApiException.conflicto("El kilometraje no puede ser menor al último registrado: " + ultima.get().getKilometraje());
        OrdenServicio orden = new OrdenServicio();
        orden.setVehiculo(v);
        orden.setKilometraje(dto.kilometraje());
        orden.setFecha(LocalDateTime.now(clock));
        orden.setEstado("REGISTRADA");
        orden.setUsuarioRegistro(username);
        Set<Long> usados = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleOrdenRequestDTO d : dto.detalles()) {
            if (d == null || d.servicioId() == null || d.cantidad() == null || d.cantidad() < 1 || d.cantidad() > 5)
                throw ApiException.invalido("Cada detalle requiere servicio y cantidad entre 1 y 5");
            if (!usados.add(d.servicioId())) throw ApiException.invalido("No se puede repetir un servicio en la orden");
            Servicio s = servicios.findById(d.servicioId()).orElseThrow(() -> ApiException.noExiste("Servicio"));
            if (!Boolean.TRUE.equals(s.getActivo()))
                throw ApiException.conflicto("El servicio " + s.getCodigo() + " está inactivo");
            DetalleOrden detalle = new DetalleOrden();
            detalle.setOrden(orden);
            detalle.setServicio(s);
            detalle.setCantidad(d.cantidad());
            detalle.setPrecioUnitario(s.getPrecio());
            detalle.setSubtotal(s.getPrecio().multiply(BigDecimal.valueOf(d.cantidad())));
            orden.getDetalles().add(detalle);
            total = total.add(detalle.getSubtotal());
        }
        if (total.compareTo(new BigDecimal("99999999.99")) > 0)
            throw ApiException.invalido("El total supera el importe máximo permitido");
        // Consume the independent sequence only after all business validations succeed.
        orden.setNumero("OS-%06d".formatted(ordenes.siguienteNumero()));
        orden.setTotal(total);
        ordenes.saveAndFlush(orden);
        log.info("Orden registrada id={} numero={} detalles={}", orden.getId(), orden.getNumero(), orden.getDetalles().size());
        return mapper.respuesta(orden);
    }

    @Transactional
    public OrdenResponseDTO anular(Long id, String motivo) {
        if (motivo == null || motivo.isBlank() || motivo.length() < 10 || motivo.length() > 200)
            throw ApiException.invalido("El motivo debe tener entre 10 y 200 caracteres");
        OrdenServicio orden = ordenes.findForUpdateById(id).orElseThrow(() -> ApiException.noExiste("Orden"));
        if (!"REGISTRADA".equals(orden.getEstado()))
            throw ApiException.conflicto("Solo se puede anular una orden REGISTRADA");
        if (!orden.getFecha().toLocalDate().equals(LocalDate.now(clock)))
            throw ApiException.conflicto("Solo se puede anular una orden el mismo día de su registro");
        orden.setEstado("ANULADA");
        orden.setMotivoAnulacion(motivo);
        ordenes.flush();
        log.info("Orden anulada id={}", id);
        return mapper.respuesta(orden);
    }
}
