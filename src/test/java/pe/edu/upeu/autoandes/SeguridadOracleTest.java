package pe.edu.upeu.autoandes;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;
import pe.edu.upeu.autoandes.config.JwtService;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;
import pe.edu.upeu.autoandes.entity.Usuario;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc(print=MockMvcPrint.NONE)
class SeguridadOracleTest {
 @Autowired MockMvc mvc;@Autowired JsonMapper json;@Autowired Clock clock;
 @Autowired UsuarioRepository usuarios;@Autowired PasswordEncoder encoder;
 @Value("${app.jwt.secret}") String secret;
 private LoginResponseDTO login(String username) throws Exception {
  var result=mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(new LoginRequestDTO(username,"Auto2026*")))).andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist()).andReturn();
  return json.readValue(result.getResponse().getContentAsString(),LoginResponseDTO.class);
 }
 private ResultActions error(ResultActions action,int status,String ruta,boolean campos) throws Exception {
  action.andExpect(status().is(status)).andExpect(jsonPath("$.timestamp").exists()).andExpect(jsonPath("$.status").value(status)).andExpect(jsonPath("$.error").exists()).andExpect(jsonPath("$.mensaje").isNotEmpty()).andExpect(jsonPath("$.ruta").value(ruta))
   .andExpect(jsonPath("$.message").doesNotExist()).andExpect(jsonPath("$.path").doesNotExist()).andExpect(jsonPath("$.validationErrors").doesNotExist());
  if(!campos) action.andExpect(jsonPath("$.campos").doesNotExist());
  return action;
 }
 @Test void loginBcryptInexistenteIncorrectoEInactivoMismo401() throws Exception {
  for(String name:List.of("admin","asesor","jefe","inactivo")) assertThat(encoder.matches("Auto2026*",usuarios.findByUsername(name).orElseThrow().getPassword())).isTrue();
  String mensaje=null;
  for(var d:List.of(new LoginRequestDTO("noexiste","Auto2026*"),new LoginRequestDTO("admin","incorrecta"),new LoginRequestDTO("inactivo","Auto2026*"))) {
   var r=error(mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(d))),401,"/api/v1/auth/login",false).andReturn();
   var body=json.readValue(r.getResponse().getContentAsString(),ErrorResponseDTO.class);
   if(mensaje==null) mensaje=body.mensaje();else assertThat(body.mensaje()).isEqualTo(mensaje);
  }
  var sesion=login("admin");assertThat(sesion.tipo()).isEqualTo("Bearer");assertThat(sesion.expiraEnSegundos()).isEqualTo(3600);assertThat(sesion.permisos()).hasSize(8);
  mvc.perform(get("/api/v1/auth/me").header("Authorization","Bearer "+sesion.token())).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("admin")).andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.permisos.length()").value(8));
 }
 @Test void tokenAusenteAlteradoVencidoYFirmaAjenaSon401() throws Exception {
  for(String ruta:List.of("/api/v1/clientes","/api/v1/servicios","/api/v1/vehiculos","/api/v1/ordenes","/api/v1/reportes/ingresos-por-servicio","/api/v1/auth/me","/actuator/health")) error(mvc.perform(get(ruta)),401,ruta,false);
  String token=login("admin").token();int inicio=token.lastIndexOf('.')+1;
  String alterado=token.substring(0,inicio)+(token.charAt(inicio)=='A'?'B':'A')+token.substring(inicio+1);
  String vencido=new JwtService(secret,3600,Clock.offset(clock,Duration.ofHours(-2))).generar("admin");
  String claveAjena=Base64.getEncoder().encodeToString("clave-de-prueba-independiente-para-firma-invalida-12345".getBytes(java.nio.charset.StandardCharsets.UTF_8));
  String ajeno=new JwtService(claveAjena,3600,clock).generar("admin");
  for(String invalido:List.of(alterado,vencido,ajeno,"no-es-jwt")) error(mvc.perform(get("/api/v1/clientes").header("Authorization","Bearer "+invalido)),401,"/api/v1/clientes",false);
  error(mvc.perform(get("/api/v1/clientes").header("Authorization","Basic abc")),401,"/api/v1/clientes",false);
 }
 @Test void matrizCompletaDePermisosSeAplicaEnBackend() throws Exception {
  for(String username:List.of("admin","asesor","jefe")) {
   var sesion=login(username);String bearer="Bearer "+sesion.token();
   for(String ruta:List.of("/api/v1/clientes","/api/v1/servicios","/api/v1/vehiculos","/api/v1/ordenes","/api/v1/ordenes/1","/api/v1/vehiculos/1","/api/v1/servicios/1")) mvc.perform(get(ruta).header("Authorization",bearer)).andExpect(status().isOk());
   var rutas=List.of("/api/v1/servicios","/api/v1/vehiculos","/api/v1/ordenes");
   var permisos=List.of("SERVICIO_ESCRIBIR","VEHICULO_ESCRIBIR","ORDEN_REGISTRAR");
   for(int i=0;i<rutas.size();i++) error(mvc.perform(post(rutas.get(i)).header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content("{}")),sesion.permisos().contains(permisos.get(i))?400:403,rutas.get(i),sesion.permisos().contains(permisos.get(i)));
   String anular="/api/v1/ordenes/999999/anular";
   error(mvc.perform(patch(anular).header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Solicitud del cliente\"}")),sesion.permisos().contains("ORDEN_ANULAR")?404:403,anular,false);
   var reporte=mvc.perform(get("/api/v1/reportes/ingresos-por-servicio").header("Authorization",bearer));
   if(sesion.permisos().contains("REPORTE_VER")) reporte.andExpect(status().isOk());else error(reporte,403,"/api/v1/reportes/ingresos-por-servicio",false);
  }
 }
 @Test @Transactional void variosRolesRecibenUnionRealDePermisos() throws Exception {
  var admin=usuarios.findByUsername("admin").orElseThrow();var asesor=usuarios.findByUsername("asesor").orElseThrow();var jefe=usuarios.findByUsername("jefe").orElseThrow();
  Usuario multi=new Usuario();multi.setUsername("multi_prueba");multi.setNombreCompleto("Prueba múltiples roles");multi.setPassword(admin.getPassword());multi.setActivo(true);multi.setRoles(new HashSet<>(asesor.getRoles()));multi.getRoles().addAll(jefe.getRoles());usuarios.saveAndFlush(multi);
  assertThat(login("multi_prueba").permisos()).containsExactlyInAnyOrderElementsOf(login("admin").permisos());
 }
 @Test void validacionAnidadaFechasErrores404409YCors() throws Exception {
  String bearer="Bearer "+login("admin").token();
  error(mvc.perform(post("/api/v1/vehiculos").header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content("{\"placa\":\"abc\",\"marca\":\"Kia\",\"modelo\":\"Rio\",\"anio\":1980,\"clienteId\":1}")),400,"/api/v1/vehiculos",true).andExpect(jsonPath("$.campos.placa").exists()).andExpect(jsonPath("$.campos.anio").exists());
  error(mvc.perform(post("/api/v1/ordenes").header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content("{\"vehiculoId\":1,\"kilometraje\":45000,\"detalles\":[{\"servicioId\":1,\"cantidad\":6}]}")),400,"/api/v1/ordenes",true).andExpect(jsonPath("$.campos['detalles[0].cantidad']").exists());
  error(mvc.perform(get("/api/v1/vehiculos/999999").header("Authorization",bearer)),404,"/api/v1/vehiculos/999999",false);
  error(mvc.perform(delete("/api/v1/servicios/1").header("Authorization",bearer)),409,"/api/v1/servicios/1",false);
  error(mvc.perform(get("/api/v1/ordenes?desde=2026-10-30&hasta=2026-10-29").header("Authorization",bearer)),400,"/api/v1/ordenes",false);
  error(mvc.perform(get("/api/v1/ordenes?desde=no-fecha").header("Authorization",bearer)),400,"/api/v1/ordenes",false);
  mvc.perform(options("/api/v1/ordenes").header("Origin","http://localhost:4200").header("Access-Control-Request-Method","POST").header("Access-Control-Request-Headers","Authorization,Content-Type")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:4200"));
  error(mvc.perform(options("/api/v1/ordenes").header("Origin","http://origen-no-permitido.example").header("Access-Control-Request-Method","POST")),403,"/api/v1/ordenes",false);
  mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
 }
}
