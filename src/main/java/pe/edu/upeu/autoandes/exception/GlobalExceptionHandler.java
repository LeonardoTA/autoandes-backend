package pe.edu.upeu.autoandes.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import pe.edu.upeu.autoandes.dto.ErrorResponseDTO;
@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {
 private ResponseEntity<ErrorResponseDTO> error(HttpStatus status,String mensaje,HttpServletRequest req,Map<String,String> campos) {
  if(status.is4xxClientError()) log.warn("Petición rechazada: estado={} ruta={}",status.value(),req.getRequestURI());
  return ResponseEntity.status(status).body(new ErrorResponseDTO(LocalDateTime.now(ZoneId.of("America/Lima")),status.value(),status.getReasonPhrase(),mensaje,req.getRequestURI(),campos));
 }
 @ExceptionHandler(ApiException.class) ResponseEntity<ErrorResponseDTO> dominio(ApiException e,HttpServletRequest r) {return error(e.getStatus(),e.getMessage(),r,null);}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorResponseDTO> validacion(MethodArgumentNotValidException e,HttpServletRequest r) {
  Map<String,String> campos=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(f->campos.putIfAbsent(f.getField(),f.getDefaultMessage()));
  return error(HttpStatus.BAD_REQUEST,"Datos inválidos",r,campos);
 }
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,org.springframework.web.bind.MissingServletRequestParameterException.class})
 ResponseEntity<ErrorResponseDTO> formato(Exception e,HttpServletRequest r) {return error(HttpStatus.BAD_REQUEST,"Formato de datos o fecha inválido",r,null);}
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ErrorResponseDTO> integridad(Exception e,HttpServletRequest r) {return error(HttpStatus.CONFLICT,"El registro está duplicado o mantiene referencias históricas",r,null);}
 @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class) ResponseEntity<ErrorResponseDTO> inexistente(Exception e,HttpServletRequest r) {return error(HttpStatus.NOT_FOUND,"Ruta no encontrada",r,null);}
 @ExceptionHandler(org.springframework.security.core.AuthenticationException.class) ResponseEntity<ErrorResponseDTO> autenticacion(Exception e,HttpServletRequest r) {return error(HttpStatus.UNAUTHORIZED,"Credenciales inválidas",r,null);}
 @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class) ResponseEntity<ErrorResponseDTO> permiso(Exception e,HttpServletRequest r) {return error(HttpStatus.FORBIDDEN,"No tiene permiso para realizar esta operación",r,null);}
 @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class) ResponseEntity<ErrorResponseDTO> metodo(Exception e,HttpServletRequest r) {return error(HttpStatus.METHOD_NOT_ALLOWED,"Método no permitido",r,null);}
 @ExceptionHandler(Exception.class) ResponseEntity<ErrorResponseDTO> inesperado(Exception e,HttpServletRequest r) {
  log.error("Error inesperado en {} ({})",r.getRequestURI(),e.getClass().getSimpleName());
  return error(HttpStatus.INTERNAL_SERVER_ERROR,"Ocurrió un error inesperado",r,null);
 }
}
