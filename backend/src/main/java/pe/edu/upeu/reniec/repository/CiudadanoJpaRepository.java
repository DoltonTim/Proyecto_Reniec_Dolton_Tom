package pe.edu.upeu.reniec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.reniec.entity.CiudadanoEntity;

import java.util.Optional;

public interface CiudadanoJpaRepository extends JpaRepository<CiudadanoEntity, Long> {
    Optional<CiudadanoEntity> findByDni(String dni);
    boolean existsByDni(String dni);
}
