package pe.edu.upeu.autoandes.service;

import java.time.*;
import pe.edu.upeu.autoandes.exception.ApiException;
/** Inclusive calendar dates translated to an exclusive upper timestamp in Lima. */
public record RangoFechas(LocalDateTime desde,LocalDateTime hasta) {
 public static RangoFechas de(LocalDate desde,LocalDate hasta) {
  if(desde!=null && hasta!=null && desde.isAfter(hasta)) throw ApiException.invalido("La fecha desde no puede ser posterior a hasta");
  try {return new RangoFechas(desde==null?null:desde.atStartOfDay(),hasta==null?null:hasta.plusDays(1).atStartOfDay());}
  catch(DateTimeException e) {throw ApiException.invalido("Rango de fechas inválido");}
 }
}
