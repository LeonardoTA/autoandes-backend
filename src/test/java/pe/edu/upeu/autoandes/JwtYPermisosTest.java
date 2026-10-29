package pe.edu.upeu.autoandes;
import java.time.*;
import java.util.List;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.JwtException;
import pe.edu.upeu.autoandes.config.JwtService;
import pe.edu.upeu.autoandes.security.PermisosPorRol;
import static org.assertj.core.api.Assertions.*;
class JwtYPermisosTest {
 private final String clave=Base64.getEncoder().encodeToString("clave-exclusiva-de-tests-unitarios-no-usar-en-entorno-real-123".getBytes(StandardCharsets.UTF_8));
 @Test void firmaExpiracionEIntegridadSeVerifican() {
  Clock reloj=Clock.fixed(Instant.parse("2026-10-29T15:00:00Z"),ZoneId.of("America/Lima"));
  JwtService jwt=new JwtService(clave,3600,reloj);String token=jwt.generar("asesor");
  assertThat(jwt.validarYObtenerUsuario(token)).isEqualTo("asesor");
  assertThatThrownBy(()->new JwtService(clave,3600,Clock.offset(reloj,Duration.ofSeconds(3601))).validarYObtenerUsuario(token)).isInstanceOf(JwtException.class);
  String otraClave=Base64.getEncoder().encodeToString("otra-clave-exclusiva-de-tests-unitarios-no-usar-en-entorno-real".getBytes(StandardCharsets.UTF_8));
  assertThatThrownBy(()->new JwtService(otraClave,3600,reloj).validarYObtenerUsuario(token)).isInstanceOf(JwtException.class);
 }
 @Test void matrizUneRolesSinDuplicadosYDesconocidosNoOtorganAcceso() {
  assertThat(PermisosPorRol.de(List.of("ASESOR","JEFE_TALLER"))).containsExactlyElementsOf(PermisosPorRol.de(List.of("ADMIN")));
  assertThat(PermisosPorRol.de(List.of("ASESOR","ASESOR"))).hasSize(5);
  assertThat(PermisosPorRol.de(List.of("DESCONOCIDO"))).isEmpty();
 }
}
