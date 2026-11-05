package pe.edu.upeu.reniec.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        // 1. Jefe de Registro Civil / Administrador: Acceso total (Crear, Editar, Atender/Aprobar, Eliminar y Recaudacion)
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("Admin123!"))
                .authorities(
                        "ROLE_ADMIN",
                        "PADRON_LEER",
                        "PADRON_ESCRIBIR",
                        "PADRON_ELIMINAR",
                        "TRAMITES_LEER",
                        "TRAMITES_ESCRIBIR",
                        "TRAMITES_APROBAR",
                        "TRAMITES_ELIMINAR",
                        "RECAUDACION_VER"
                )
                .build();

        // 2. Brigadista de Campo: Empadrona y crea/edita tramites offline/online, pero NO elimina padron ni anula registros
        UserDetails brigadista = User.builder()
                .username("dolton")
                .password(encoder.encode("Reniec2026!"))
                .authorities(
                        "ROLE_BRIGADISTA",
                        "PADRON_LEER",
                        "PADRON_ESCRIBIR",
                        "TRAMITES_LEER",
                        "TRAMITES_ESCRIBIR"
                )
                .build();

        // 3. Modulo de Consulta / Verificador: Solo busca DNIs, gira DNIe 3.0, pasa huella AFIS y consulta etapa del tramite
        UserDetails verificador = User.builder()
                .username("verificador")
                .password(encoder.encode("Verifica2026!"))
                .authorities(
                        "ROLE_VERIFICADOR",
                        "PADRON_LEER",
                        "TRAMITES_LEER"
                )
                .build();

        return new InMemoryUserDetailsManager(admin, brigadista, verificador);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            UserDetailsService userDetailsService) throws Exception {

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/health",
                                "/h2-console/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/ciudadanos/**").hasAuthority("PADRON_ELIMINAR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tramites/**").hasAuthority("TRAMITES_ELIMINAR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/ciudadanos/**").hasAuthority("PADRON_ESCRIBIR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/ciudadanos/**").hasAuthority("PADRON_ESCRIBIR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/tramites/**").hasAuthority("TRAMITES_ESCRIBIR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tramites/**").hasAuthority("TRAMITES_ESCRIBIR")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
