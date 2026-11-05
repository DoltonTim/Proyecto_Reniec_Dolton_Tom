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

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .expiraEnSegundos(jwtService.getExpiracionSegundos())
                .username(user.getUsername())
                .nombreCompleto("Registrador Dolton Tom Meza")
                .cargo("Registrador Civil RENIEC")
                .sede("Brigada Movil UPeU - Nana")
                .roles(List.of("ROLE_ADMIN"))
                .permisos(permisos)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me(Authentication authentication) {
        List<String> permisos = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        UsuarioResponseDTO dto = UsuarioResponseDTO.builder()
                .username(authentication.getName())
                .empleadoNombre("Registrador Dolton Tom Meza")
                .cargo("Registrador Civil RENIEC")
                .sede("Brigada Movil UPeU - Nana")
                .permisos(permisos)
                .build();
        return ResponseEntity.ok(dto);
    }
}
