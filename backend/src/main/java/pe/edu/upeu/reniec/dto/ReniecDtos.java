package pe.edu.upeu.reniec.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReniecDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequestDTO {
        @NotBlank(message = "El usuario es obligatorio")
        private String username;

        @NotBlank(message = "La contrasena es obligatoria")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginResponseDTO {
        private String token;
        private String tipo;
        private long expiraEnSegundos;
        private String username;
        private String nombreCompleto;
        private String cargo;
        private String sede;
        private java.util.List<String> roles;
        private java.util.List<String> permisos;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsuarioResponseDTO {
        private String username;
        private String empleadoNombre;
        private String cargo;
        private String sede;
        private java.util.List<String> permisos;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CiudadanoDTO {
        private Long id;

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos numericos")
        private String dni;

        @NotBlank(message = "Nombres obligatorios")
        private String nombres;

        @NotBlank(message = "Apellido paterno obligatorio")
        private String apellidoPaterno;

        @NotBlank(message = "Apellido materno obligatorio")
        private String apellidoMaterno;

        @NotBlank(message = "Fecha de nacimiento obligatoria")
        private String fechaNacimiento;

        @NotBlank(message = "Estado civil obligatorio")
        private String estadoCivil;

        @NotBlank(message = "Ubigeo obligatorio")
        private String ubigeo;

        @NotBlank(message = "Direccion obligatoria")
        private String direccion;

        private boolean donacionOrganos;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TramiteDTO {
        private Long id;

        @NotBlank(message = "El DNI del ciudadano es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos numericos")
        private String dniCiudadano;

        @NotBlank(message = "El nombre del ciudadano es obligatorio")
        private String nombreCiudadano;

        @NotBlank(message = "El tipo de tramite es obligatorio")
        private String tipoTramite;

        @NotBlank(message = "El estado es obligatorio")
        private String estado;

        @NotBlank(message = "El ubigeo de destino es obligatorio")
        private String ubigeoDestino;

        @NotBlank(message = "Las observaciones son obligatorias")
        private String observaciones;

        private String fechaRegistro;
    }
}
