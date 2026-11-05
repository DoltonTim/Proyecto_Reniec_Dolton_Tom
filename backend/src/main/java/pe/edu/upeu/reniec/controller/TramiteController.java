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
import pe.edu.upeu.reniec.dto.ReniecDtos.TramiteDTO;
import pe.edu.upeu.reniec.entity.TramiteEntity;
import pe.edu.upeu.reniec.repository.TramiteJpaRepository;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tramites")
public class TramiteController {

    private final TramiteJpaRepository repository;

    public TramiteController(TramiteJpaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TramiteDTO> listar() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public TramiteDTO obtener(@PathVariable Long id) {
        TramiteEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tramite no encontrado"));
        return toDto(entity);
    }

    @PostMapping
    public ResponseEntity<TramiteDTO> crear(@Valid @RequestBody TramiteDTO dto) {
        TramiteEntity entity = new TramiteEntity();
        actualizarCampos(entity, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(repository.save(entity)));
    }

    @PutMapping("/{id}")
    public TramiteDTO actualizar(@PathVariable Long id, @Valid @RequestBody TramiteDTO dto) {
        TramiteEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tramite no encontrado"));
        actualizarCampos(entity, dto);
        return toDto(repository.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tramite no encontrado");
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void actualizarCampos(TramiteEntity entity, TramiteDTO dto) {
        entity.setDniCiudadano(dto.getDniCiudadano());
        entity.setNombreCiudadano(dto.getNombreCiudadano());
        entity.setTipoTramite(dto.getTipoTramite());
        entity.setEstado(dto.getEstado());
        entity.setUbigeoDestino(dto.getUbigeoDestino());
        entity.setObservaciones(dto.getObservaciones());
        entity.setFechaRegistro(
                dto.getFechaRegistro() != null && !dto.getFechaRegistro().isBlank()
                        ? dto.getFechaRegistro()
                        : LocalDate.now().toString()
        );
    }

    private TramiteDTO toDto(TramiteEntity e) {
        return TramiteDTO.builder()
                .id(e.getId())
                .dniCiudadano(e.getDniCiudadano())
                .nombreCiudadano(e.getNombreCiudadano())
                .tipoTramite(e.getTipoTramite())
                .estado(e.getEstado())
                .ubigeoDestino(e.getUbigeoDestino())
                .observaciones(e.getObservaciones())
                .fechaRegistro(e.getFechaRegistro())
                .build();
    }
}
