package pe.edu.upeu.reniec;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pe.edu.upeu.reniec.entity.CiudadanoEntity;
import pe.edu.upeu.reniec.entity.TramiteEntity;
import pe.edu.upeu.reniec.repository.CiudadanoJpaRepository;
import pe.edu.upeu.reniec.repository.TramiteJpaRepository;

import java.util.List;

@SpringBootApplication
public class ReniecBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReniecBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner inicializarDatosSemilla(
            CiudadanoJpaRepository ciudadanoRepo,
            TramiteJpaRepository tramiteRepo) {
        return args -> {
            if (ciudadanoRepo.count() == 0) {
                ciudadanoRepo.saveAll(List.of(
                        CiudadanoEntity.builder()
                                .dni("72345678")
                                .nombres("DOLTON TOM")
                                .apellidoPaterno("MEZA")
                                .apellidoMaterno("QUISPE")
                                .fechaNacimiento("2001-05-14")
                                .estadoCivil("SOLTERO")
                                .ubigeo("LIMA / LIMA / LURIGANCHO-CHOSICA")
                                .direccion("CARRETERA CENTRAL KM 19.5 VILLA UNION - NANA")
                                .donacionOrganos(true)
                                .build(),
                        CiudadanoEntity.builder()
                                .dni("45896321")
                                .nombres("MARIA ELENA")
                                .apellidoPaterno("PAREDES")
                                .apellidoMaterno("MENDOZA")
                                .fechaNacimiento("1995-08-22")
                                .estadoCivil("CASADO")
                                .ubigeo("JUNIN / HUANCAYO / EL TAMBO")
                                .direccion("AV. REAL 1240 URB. SAN CARLOS")
                                .donacionOrganos(true)
                                .build(),
                        CiudadanoEntity.builder()
                                .dni("70124589")
                                .nombres("JORGE LUIS")
                                .apellidoPaterno("HUAMAN")
                                .apellidoMaterno("ROJAS")
                                .fechaNacimiento("1998-11-03")
                                .estadoCivil("SOLTERO")
                                .ubigeo("PUNO / SAN ROMAN / JULIACA")
                                .direccion("JR. MARIANO NUNEZ 450")
                                .donacionOrganos(false)
                                .build(),
                        CiudadanoEntity.builder()
                                .dni("41258796")
                                .nombres("ROSA VICTORIA")
                                .apellidoPaterno("CASTILLO")
                                .apellidoMaterno("VARGAS")
                                .fechaNacimiento("1989-02-19")
                                .estadoCivil("SOLTERO")
                                .ubigeo("SAN MARTIN / SAN MARTIN / TARAPOTO")
                                .direccion("JR. JIMENEZ PIMENTEL 890")
                                .donacionOrganos(true)
                                .build(),
                        CiudadanoEntity.builder()
                                .dni("76543210")
                                .nombres("CARLOS ALBERTO")
                                .apellidoPaterno("SANCHEZ")
                                .apellidoMaterno("GAMARRA")
                                .fechaNacimiento("2003-07-30")
                                .estadoCivil("SOLTERO")
                                .ubigeo("LIMA / LIMA / ATE")
                                .direccion("AV. NICOLAS AYLLON 3420 SANTA CLARA")
                                .donacionOrganos(true)
                                .build()
                ));
            }

            if (tramiteRepo.count() == 0) {
                tramiteRepo.saveAll(List.of(
                        TramiteEntity.builder()
                                .dniCiudadano("72345678")
                                .nombreCiudadano("DOLTON TOM MEZA QUISPE")
                                .tipoTramite("RENOVACION")
                                .estado("ATENDIDO")
                                .ubigeoDestino("LIMA / LURIGANCHO - AGENCIA NANA")
                                .observaciones("Renovacion a DNI Electronico 3.0 con firma digital activa")
                                .fechaRegistro("2026-11-01")
                                .build(),
                        TramiteEntity.builder()
                                .dniCiudadano("45896321")
                                .nombreCiudadano("MARIA ELENA PAREDES MENDOZA")
                                .tipoTramite("CAMBIO_DOMICILIO")
                                .estado("EN_PROCESO")
                                .ubigeoDestino("JUNIN / HUANCAYO")
                                .observaciones("Actualizacion de direccion domiciliaria con recibo verificado")
                                .fechaRegistro("2026-11-02")
                                .build(),
                        TramiteEntity.builder()
                                .dniCiudadano("70124589")
                                .nombreCiudadano("JORGE LUIS HUAMAN ROJAS")
                                .tipoTramite("DUPLICADO")
                                .estado("PENDIENTE")
                                .ubigeoDestino("PUNO / JULIACA")
                                .observaciones("Solicitud de duplicado por perdida en campana itinerante")
                                .fechaRegistro("2026-11-03")
                                .build()
                ));
            }
        };
    }
}
