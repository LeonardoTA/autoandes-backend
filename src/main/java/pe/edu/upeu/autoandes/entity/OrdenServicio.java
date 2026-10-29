package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orden_servicio")
@Getter
@Setter
@NoArgsConstructor
public class OrdenServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(precision = 38, scale = 0)
    private Long id;
    @Column(nullable = false, length = 9, unique = true)
    private String numero;
    @Column(nullable = false)
    private java.time.LocalDateTime fecha;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;
    @Column(nullable = false, precision = 7)
    private Integer kilometraje;
    @Column(nullable = false, length = 12)
    private String estado;
    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal total;
    @Column(name = "usuario_registro", nullable = false, length = 30)
    private String usuarioRegistro;
    @Column(name = "motivo_anulacion", length = 200)
    private String motivoAnulacion;
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private java.util.List<DetalleOrden> detalles = new java.util.ArrayList<>();
}
