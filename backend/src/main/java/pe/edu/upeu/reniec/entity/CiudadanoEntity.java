package pe.edu.upeu.reniec.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ciudadanos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CiudadanoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Column(nullable = false, length = 120)
    private String nombres;

    @Column(nullable = false, length = 80)
    private String apellidoPaterno;

    @Column(nullable = false, length = 80)
    private String apellidoMaterno;

    @Column(nullable = false, length = 20)
    private String fechaNacimiento;

    @Column(nullable = false, length = 30)
    private String estadoCivil;

    @Column(nullable = false, length = 150)
    private String ubigeo;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false)
    private boolean donacionOrganos;
}
