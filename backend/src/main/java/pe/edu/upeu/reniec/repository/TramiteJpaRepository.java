package pe.edu.upeu.reniec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.reniec.entity.TramiteEntity;

import java.util.List;

public interface TramiteJpaRepository extends JpaRepository<TramiteEntity, Long> {
    List<TramiteEntity> findByDniCiudadanoOrderByIdDesc(String dniCiudadano);
}
