package pe.edu.upeu.autoandes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.service.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.exception.ApiException;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE)
@Transactional
class CatalogoOracleTest {
 @Autowired ServicioService servicios;
 @Autowired VehiculoService vehiculos;
 @Autowired ClienteService clientes;
 @Test void semillasYCrudReal() {
  assertThat(clientes.listar()).hasSizeGreaterThanOrEqualTo(6);
  var s=servicios.crear(new ServicioRequestDTO("SRV-990","Prueba integración",new BigDecimal("19.90"),30,true));
  assertThat(servicios.obtener(s.id()).precio()).isEqualByComparingTo("19.90");
  assertThat(servicios.actualizar(s.id(),new ServicioRequestDTO("SRV-990","Prueba editada",new BigDecimal("29.90"),45,true)).nombre()).isEqualTo("Prueba editada");
  assertThat(servicios.estado(s.id(),false).activo()).isFalse();
  servicios.eliminar(s.id());
  assertThatThrownBy(()->servicios.obtener(s.id())).isInstanceOf(ApiException.class).extracting("status.value").isEqualTo(404);
  var v=vehiculos.crear(new VehiculoRequestDTO("ZZZ-990","Toyota","Yaris",2026,1L));
  assertThat(v.clienteNombre()).isNotBlank();
  assertThat(vehiculos.actualizar(v.id(),new VehiculoRequestDTO("ZZZ-990","Kia","Rio",2025,2L)).clienteId()).isEqualTo(2L);
  vehiculos.eliminar(v.id());
 }
 @Test void historiaYUnicidadSePreservan() {
  assertThatThrownBy(()->servicios.eliminar(1L)).isInstanceOf(ApiException.class).hasMessageContaining("baja");
  assertThatThrownBy(()->vehiculos.eliminar(1L)).isInstanceOf(ApiException.class).hasMessageContaining("órdenes");
  assertThatThrownBy(()->servicios.crear(new ServicioRequestDTO("SRV-001","Duplicado",BigDecimal.TEN,30,true))).isInstanceOf(ApiException.class).hasMessageContaining("existe");
  assertThatThrownBy(()->vehiculos.crear(new VehiculoRequestDTO("ZZZ-991","Kia","Rio",2025,999999L))).isInstanceOf(ApiException.class).hasMessageContaining("Cliente");
 }
}
