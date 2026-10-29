package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.dto.*;
@Component
public class OrdenMapper {
 public OrdenResponseDTO respuesta(OrdenServicio o) {
  Vehiculo v=o.getVehiculo();
  return new OrdenResponseDTO(o.getId(),o.getNumero(),o.getFecha(),v.getId(),v.getPlaca(),v.getCliente().getNombres(),o.getKilometraje(),o.getEstado(),o.getTotal(),o.getUsuarioRegistro(),o.getMotivoAnulacion(),o.getDetalles().stream().map(this::detalle).toList());
 }
 private DetalleOrdenResponseDTO detalle(DetalleOrden d) {Servicio s=d.getServicio();return new DetalleOrdenResponseDTO(d.getId(),s.getId(),s.getCodigo(),s.getNombre(),d.getCantidad(),d.getPrecioUnitario(),d.getSubtotal());}
}
