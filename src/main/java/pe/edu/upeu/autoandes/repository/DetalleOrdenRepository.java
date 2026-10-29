package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.autoandes.entity.DetalleOrden;
import pe.edu.upeu.autoandes.dto.IngresoServicioDTO;
import java.time.LocalDateTime;
import java.util.List;
public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden,Long> {
 boolean existsByServicioId(Long servicioId);
 @Query("""
  select new pe.edu.upeu.autoandes.dto.IngresoServicioDTO(s.codigo,s.nombre,sum(d.cantidad),sum(d.subtotal))
  from DetalleOrden d join d.servicio s join d.orden o
  where o.estado='REGISTRADA'
  and (:desde is null or o.fecha >= :desde)
  and (:hasta is null or o.fecha < :hasta)
  group by s.codigo,s.nombre
  order by sum(d.subtotal) desc
  """)
 List<IngresoServicioDTO> ingresosPorServicio(@Param("desde") LocalDateTime desde,@Param("hasta") LocalDateTime hasta);
}
