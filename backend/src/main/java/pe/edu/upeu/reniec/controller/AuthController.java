package pe.edu.upeu.reniec.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.reniec.dto.ReniecDtos.LoginRequestDTO;
import pe.edu.upeu.reniec.dto.ReniecDtos.LoginResponseDTO;
import pe.edu.upeu.reniec.dto.ReniecDtos.UsuarioResponseDTO;
import pe.edu.upeu.reniec.security.JwtService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        String token = jwtService.generarToken(user);

        List<String> permisos = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        PerfilMetadata meta = obtenerMetadata(user.getUsername());

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .expiraEnSegundos(jwtService.getExpiracionSegundos())
                .username(user.getUsername())
                .nombreCompleto(meta.nombreCompleto)
                .cargo(meta.cargo)
                .sede(meta.sede)
                .roles(List.of(meta.rol))
                .permisos(permisos)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me(Authentication authentication) {
        List<String> permisos = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        PerfilMetadata meta = obtenerMetadata(authentication.getName());

        UsuarioResponseDTO dto = UsuarioResponseDTO.builder()
                .username(authentication.getName())
                .empleadoNombre(meta.nombreCompleto)
                .cargo(meta.cargo)
                .sede(meta.sede)
                .permisos(permisos)
                .build();
        return ResponseEntity.ok(dto);
    }

    private PerfilMetadata obtenerMetadata(String username) {
        String u = username == null ? "" : username.toLowerCase();
        return switch (u) {
            case "admin" -> new PerfilMetadata(
                    "Ing. Carlos Mendoza (Administrador RENIEC)",
                    "Jefe de Registro Civil - Control Total",
                    "Sede Central RENIEC Lima",
                    "ROLE_ADMIN"
            );
            case "dolton" -> new PerfilMetadata(
                    "Brigadista Dolton Tom Meza",
                    "Brigadista de Campo (Offline/Online)",
                    "Brigada Movil UPeU - Nana",
                    "ROLE_BRIGADISTA"
            );
            default -> new PerfilMetadata(
                    "Lic. Ana Torres (Modulo Consulta)",
                    "Verificador Biometrico AFIS y Ventanilla",
                    "Agencia RENIEC Ate - Vitarte",
                    "ROLE_VERIFICADOR"
            );
        };
    }

    private record PerfilMetadata(
            String nombreCompleto,
            String cargo,
            String sede,
            String rol
    ) {}
}
