package pe.edu.upeu.autoandes;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.service.*;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.exception.ApiException;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE)
class OrdenOracleTest {
 @Autowired OrdenService service;@Autowired ReporteService reportes;
 @Autowired OrdenServicioRepository ordenes;@Autowired DetalleOrdenRepository detalles;
 @Autowired PlatformTransactionManager manager;@Autowired Clock clock;
 private OrdenRequestDTO solicitud(List<DetalleOrdenRequestDTO> ds) {return new OrdenRequestDTO(1L,45000,ds);}
 @Test void tercerDetalleInactivoNoDejaFilas() {
  long cabeceras=ordenes.count(),lineas=detalles.count();
  assertThatThrownBy(()->service.registrar(solicitud(List.of(new DetalleOrdenRequestDTO(1L,1),new DetalleOrdenRequestDTO(2L,1),new DetalleOrdenRequestDTO(8L,1))),"admin")).isInstanceOf(ApiException.class).hasMessageContaining("inactivo");
  assertThat(ordenes.count()).isEqualTo(cabeceras);assertThat(detalles.count()).isEqualTo(lineas);
 }
 @Test void calculoServidorFiltrosYAnulacionMismoDia() {
  new TransactionTemplate(manager).executeWithoutResult(tx->{
   var hoy=LocalDate.now(clock);
   var base=reportes.ingresosPorServicio(hoy,hoy).stream().filter(r->r.codigo().equals("SRV-001")).map(IngresoServicioDTO::importeTotal).findFirst().orElse(BigDecimal.ZERO);
   var o=service.registrar(solicitud(List.of(new DetalleOrdenRequestDTO(1L,2),new DetalleOrdenRequestDTO(2L,1))),"asesor");
   assertThat(o.numero()).matches("OS-\\d{6}");assertThat(o.estado()).isEqualTo("REGISTRADA");assertThat(o.usuarioRegistro()).isEqualTo("asesor");
   assertThat(o.total()).isEqualByComparingTo("320.00");assertThat(o.detalles()).hasSize(2);assertThat(o.detalles().getFirst().precioUnitario()).isEqualByComparingTo("120.00");
   assertThat(service.listar("aaa","REGISTRADA",hoy,hoy)).extracting(OrdenResponseDTO::id).contains(o.id());
   assertThat(reportes.ingresosPorServicio(hoy,hoy)).filteredOn(r->r.codigo().equals("SRV-001")).singleElement().satisfies(r->assertThat(r.importeTotal()).isEqualByComparingTo(base.add(new BigDecimal("240.00"))));
   assertThat(service.anular(o.id(),"Solicitud del cliente").estado()).isEqualTo("ANULADA");
   var despues=reportes.ingresosPorServicio(hoy,hoy).stream().filter(r->r.codigo().equals("SRV-001")).map(IngresoServicioDTO::importeTotal).findFirst().orElse(BigDecimal.ZERO);
   assertThat(despues).isEqualByComparingTo(base);tx.setRollbackOnly();
  });
 }
 @Test void rechazaKilometrajeRepetidosCantidadYAnulacionFueraDeDia() {
  assertThatThrownBy(()->service.registrar(new OrdenRequestDTO(1L,44999,List.of(new DetalleOrdenRequestDTO(1L,1))),"admin")).hasMessageContaining("45000");
  assertThatThrownBy(()->service.registrar(solicitud(List.of(new DetalleOrdenRequestDTO(1L,1),new DetalleOrdenRequestDTO(1L,2))),"admin")).hasMessageContaining("repetir");
  assertThatThrownBy(()->service.registrar(solicitud(List.of(new DetalleOrdenRequestDTO(1L,6))),"admin")).hasMessageContaining("cantidad");
  assertThatThrownBy(()->service.anular(1L,"Solicitud del cliente")).hasMessageContaining("mismo día");
  assertThatThrownBy(()->service.anular(4L,"Solicitud del cliente")).hasMessageContaining("REGISTRADA");
  assertThatThrownBy(()->service.anular(1L,"corto")).hasMessageContaining("10 y 200");
  assertThatThrownBy(()->service.listar(null,null,LocalDate.of(2026,10,30),LocalDate.of(2026,10,29))).hasMessageContaining("posterior");
 }
 @Test void reporteSemillaExcluyeAnuladaYOrdenaImporte() {
  var rows=reportes.ingresosPorServicio(LocalDate.of(2026,10,28),LocalDate.of(2026,10,28));
  assertThat(rows).hasSize(4);assertThat(rows).extracting(IngresoServicioDTO::importeTotal).isSortedAccordingTo(java.util.Comparator.reverseOrder());
  assertThat(rows).filteredOn(r->r.codigo().equals("SRV-001")).singleElement().satisfies(r->{assertThat(r.cantidadTotal()).isEqualTo(1);assertThat(r.importeTotal()).isEqualByComparingTo("120.00");});
 }
}
