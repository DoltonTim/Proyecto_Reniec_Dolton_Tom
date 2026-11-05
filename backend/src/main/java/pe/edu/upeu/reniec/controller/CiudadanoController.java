package pe.edu.upeu.reniec.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.reniec.dto.ReniecDtos.CiudadanoDTO;
import pe.edu.upeu.reniec.entity.CiudadanoEntity;
import pe.edu.upeu.reniec.repository.CiudadanoJpaRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ciudadanos")
public class CiudadanoController {

    private final CiudadanoJpaRepository repository;

    public CiudadanoController(CiudadanoJpaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<CiudadanoDTO> listar() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/dni/{dni}")
    public CiudadanoDTO buscarPorDni(@PathVariable String dni) {
        CiudadanoEntity entity = repository.findByDni(dni)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ciudadano no encontrado"));
        return toDto(entity);
    }

    @PostMapping
    public ResponseEntity<CiudadanoDTO> crear(@Valid @RequestBody CiudadanoDTO dto) {
        CiudadanoEntity entity = repository.findByDni(dto.getDni())
                .map(existente -> {
                    actualizarCampos(existente, dto);
                    return repository.save(existente);
                })
                .orElseGet(() -> {
                    CiudadanoEntity nuevo = new CiudadanoEntity();
                    actualizarCampos(nuevo, dto);
                    return repository.save(nuevo);
                });
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(entity));
    }

    @PutMapping("/{id}")
    public CiudadanoDTO actualizar(@PathVariable Long id, @Valid @RequestBody CiudadanoDTO dto) {
        CiudadanoEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ciudadano no encontrado"));
        actualizarCampos(entity, dto);
        return toDto(repository.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ciudadano no encontrado");
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void actualizarCampos(CiudadanoEntity entity, CiudadanoDTO dto) {
        entity.setDni(dto.getDni());
        entity.setNombres(dto.getNombres());
        entity.setApellidoPaterno(dto.getApellidoPaterno());
        entity.setApellidoMaterno(dto.getApellidoMaterno());
        entity.setFechaNacimiento(dto.getFechaNacimiento());
        entity.setEstadoCivil(dto.getEstadoCivil());
        entity.setUbigeo(dto.getUbigeo());
        entity.setDireccion(dto.getDireccion());
        entity.setDonacionOrganos(dto.isDonacionOrganos());
    }

    private CiudadanoDTO toDto(CiudadanoEntity e) {
        return CiudadanoDTO.builder()
                .id(e.getId())
                .dni(e.getDni())
                .nombres(e.getNombres())
                .apellidoPaterno(e.getApellidoPaterno())
                .apellidoMaterno(e.getApellidoMaterno())
                .fechaNacimiento(e.getFechaNacimiento())
                .estadoCivil(e.getEstadoCivil())
                .ubigeo(e.getUbigeo())
                .direccion(e.getDireccion())
                .donacionOrganos(e.isDonacionOrganos())
                .build();
    }
}
