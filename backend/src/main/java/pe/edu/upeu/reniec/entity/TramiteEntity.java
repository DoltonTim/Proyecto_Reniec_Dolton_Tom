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
@Table(name = "tramites_dni")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String dniCiudadano;

    @Column(nullable = false, length = 180)
    private String nombreCiudadano;

    @Column(nullable = false, length = 50)
    private String tipoTramite;

    @Column(nullable = false, length = 40)
    private String estado;

    @Column(nullable = false, length = 160)
    private String ubigeoDestino;

    @Column(nullable = false, length = 400)
    private String observaciones;

    @Column(nullable = false, length = 25)
    private String fechaRegistro;
}
