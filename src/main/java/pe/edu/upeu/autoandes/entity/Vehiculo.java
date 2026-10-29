package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
@Entity @Table(name="vehiculo") @Getter @Setter @NoArgsConstructor
public class Vehiculo {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(precision=38,scale=0) private Long id;
 @Column(nullable=false,length=7,unique=true) private String placa;
 @Column(nullable=false,length=40) private String marca;
 @Column(nullable=false,length=40) private String modelo;
 @Column(nullable=false,precision=4) private Integer anio;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="cliente_id") private Cliente cliente;
}
