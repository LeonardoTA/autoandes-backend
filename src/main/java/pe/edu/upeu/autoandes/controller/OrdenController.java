package pe.edu.upeu.autoandes.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.exception.ApiException;
import pe.edu.upeu.autoandes.service.OrdenService;
@RestController @RequestMapping("/api/v1/ordenes") @RequiredArgsConstructor
public class OrdenController {
 private final OrdenService service;
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public OrdenResponseDTO registrar(@Valid @RequestBody OrdenRequestDTO d,Principal principal) {
  if(principal==null) throw new ApiException(HttpStatus.UNAUTHORIZED,"Se requiere autenticación");
  return service.registrar(d,principal.getName());
 }
 @GetMapping public List<OrdenResponseDTO> listar(@RequestParam(required=false) String placa,@RequestParam(required=false) String estado,
  @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate desde,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate hasta) {return service.listar(placa,estado,desde,hasta);}
 @GetMapping("/{id}") public OrdenResponseDTO obtener(@PathVariable Long id) {return service.obtener(id);}
 @PatchMapping("/{id}/anular") public OrdenResponseDTO anular(@PathVariable Long id,@Valid @RequestBody AnulacionRequestDTO d) {return service.anular(id,d.motivo());}
}
