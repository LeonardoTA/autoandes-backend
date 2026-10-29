package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(precision = 38, scale = 0)
    private Long id;
    @Column(nullable = false, length = 30, unique = true)
    private String username;
    @Column(nullable = false, length = 100)
    private String password;
    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;
    @Convert(converter = ActivoConverter.class)
    @Column(nullable = false, columnDefinition = "NUMBER(1)")
    private Boolean activo;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private java.util.Set<Rol> roles = new java.util.HashSet<>();
}
