package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
@Entity @Table(name="cliente") @Getter @Setter @NoArgsConstructor
public class Cliente {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(precision=38,scale=0) private Long id;
 @Column(nullable=false,length=11,unique=true) private String documento;
 @Column(nullable=false,length=120) private String nombres;
 @Column(length=15) private String telefono;
}
