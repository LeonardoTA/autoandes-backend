package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
@Converter
public class ActivoConverter implements AttributeConverter<Boolean,Integer> {
 public Integer convertToDatabaseColumn(Boolean value) { return value==null ? null : (value ? 1 : 0); }
 public Boolean convertToEntityAttribute(Integer value) { return value==null ? null : value==1; }
}
