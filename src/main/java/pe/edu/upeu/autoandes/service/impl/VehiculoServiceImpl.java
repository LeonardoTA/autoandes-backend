package pe.edu.upeu.autoandes.service.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.exception.ApiException;
import pe.edu.upeu.autoandes.mapper.CatalogoMapper;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.service.VehiculoService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehiculoServiceImpl implements VehiculoService {
    private final VehiculoRepository vehiculos;
    private final ClienteRepository clientes;
    private final OrdenServicioRepository ordenes;
    private final CatalogoMapper mapper;

    private Vehiculo buscar(Long id) {
        return vehiculos.findById(id).orElseThrow(() -> ApiException.noExiste("Vehículo"));
    }

    public List<VehiculoResponseDTO> listar(Long clienteId) {
        return (clienteId == null ? vehiculos.findAll(Sort.by("placa")) : vehiculos.findByClienteIdOrderByPlacaAsc(clienteId)).stream().map(mapper::vehiculo).toList();
    }

    public VehiculoResponseDTO obtener(Long id) {
        return mapper.vehiculo(buscar(id));
    }

    @Transactional
    public VehiculoResponseDTO crear(VehiculoRequestDTO d) {
        if (vehiculos.existsByPlaca(d.placa())) throw ApiException.conflicto("La placa ya existe");
        Cliente c = clientes.findById(d.clienteId()).orElseThrow(() -> ApiException.noExiste("Cliente"));
        Vehiculo v = new Vehiculo();
        mapper.actualizar(v, d, c);
        vehiculos.saveAndFlush(v);
        log.info("Vehículo creado id={}", v.getId());
        return mapper.vehiculo(v);
    }

    @Transactional
    public VehiculoResponseDTO actualizar(Long id, VehiculoRequestDTO d) {
        Vehiculo v = buscar(id);
        if (vehiculos.existsByPlacaAndIdNot(d.placa(), id)) throw ApiException.conflicto("La placa ya existe");
        Cliente c = clientes.findById(d.clienteId()).orElseThrow(() -> ApiException.noExiste("Cliente"));
        mapper.actualizar(v, d, c);
        vehiculos.flush();
        log.info("Vehículo actualizado id={}", id);
        return mapper.vehiculo(v);
    }

    @Transactional
    public void eliminar(Long id) {
        Vehiculo v = buscar(id);
        if (ordenes.existsByVehiculoId(id))
            throw ApiException.conflicto("El vehículo tiene órdenes y no puede eliminarse");
        vehiculos.delete(v);
        vehiculos.flush();
        log.info("Vehículo eliminado id={}", id);
    }
}
