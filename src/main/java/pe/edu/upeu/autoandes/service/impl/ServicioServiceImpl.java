package pe.edu.upeu.autoandes.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.exception.ApiException;
import pe.edu.upeu.autoandes.mapper.CatalogoMapper;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.service.ServicioService;
@Service @RequiredArgsConstructor @Slf4j @Transactional(readOnly=true)
public class ServicioServiceImpl implements ServicioService {
 private final ServicioRepository servicios;private final DetalleOrdenRepository detalles;private final CatalogoMapper mapper;
 private Servicio buscar(Long id) {return servicios.findById(id).orElseThrow(()->ApiException.noExiste("Servicio"));}
 public List<ServicioResponseDTO> listar() {return servicios.findAll(Sort.by("codigo")).stream().map(mapper::servicio).toList();}
 public ServicioResponseDTO obtener(Long id) {return mapper.servicio(buscar(id));}
 @Transactional public ServicioResponseDTO crear(ServicioRequestDTO d) {
  if(servicios.existsByCodigo(d.codigo())) throw ApiException.conflicto("El código de servicio ya existe");
  Servicio s=new Servicio();mapper.actualizar(s,d);servicios.saveAndFlush(s);log.info("Servicio creado id={}",s.getId());return mapper.servicio(s);
 }
 @Transactional public ServicioResponseDTO actualizar(Long id,ServicioRequestDTO d) {
  Servicio s=buscar(id);if(servicios.existsByCodigoAndIdNot(d.codigo(),id)) throw ApiException.conflicto("El código de servicio ya existe");
  mapper.actualizar(s,d);servicios.flush();log.info("Servicio actualizado id={}",id);return mapper.servicio(s);
 }
 @Transactional public ServicioResponseDTO estado(Long id,boolean activo) {Servicio s=buscar(id);s.setActivo(activo);servicios.flush();log.info("Estado de servicio actualizado id={} activo={}",id,activo);return mapper.servicio(s);}
 @Transactional public void eliminar(Long id) {
  Servicio s=buscar(id);if(detalles.existsByServicioId(id)) throw ApiException.conflicto("El servicio tiene órdenes; use la baja de estado para conservar la historia");
  servicios.delete(s);servicios.flush();log.info("Servicio eliminado id={}",id);
 }
}
