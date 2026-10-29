package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import pe.edu.upeu.autoandes.entity.OrdenServicio;
import java.time.LocalDateTime;
import java.util.*;
public interface OrdenServicioRepository extends JpaRepository<OrdenServicio,Long> {
 boolean existsByVehiculoId(Long vehiculoId);
 Optional<OrdenServicio> findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(Long vehiculoId,String estado);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select o from OrdenServicio o where o.id=:id")
 Optional<OrdenServicio> findForUpdateById(@Param("id") Long id);
 @Query("""
  select o from OrdenServicio o join o.vehiculo v
  where (:placa is null or upper(v.placa) like concat('%',concat(upper(:placa),'%')))
  and (:estado is null or o.estado=:estado)
  and (:desde is null or o.fecha >= :desde)
  and (:hasta is null or o.fecha < :hasta)
  order by o.fecha desc,o.id desc
  """)
 List<OrdenServicio> filtrar(@Param("placa") String placa,@Param("estado") String estado,@Param("desde") LocalDateTime desde,@Param("hasta") LocalDateTime hasta);
 @Query(value="select orden_numero_seq.nextval from dual",nativeQuery=true)
 Long siguienteNumero();
}
