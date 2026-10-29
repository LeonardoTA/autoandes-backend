package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
@Entity @Table(name="roles") @Getter @Setter @NoArgsConstructor
public class Rol {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(precision=38,scale=0) private Long id;
 @Column(nullable=false,length=30,unique=true) private String nombre;
}
