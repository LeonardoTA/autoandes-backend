package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "servicio")
@Getter
@Setter
@NoArgsConstructor
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(precision = 38, scale = 0)
    private Long id;
    @Column(nullable = false, length = 7, unique = true)
    private String codigo;
    @Column(nullable = false, length = 80)
    private String nombre;
    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal precio;
    @Column(name = "duracion_min", nullable = false, precision = 4)
    private Integer duracionMin;
    @Convert(converter = ActivoConverter.class)
    @Column(nullable = false, columnDefinition = "NUMBER(1)")
    private Boolean activo;
}
