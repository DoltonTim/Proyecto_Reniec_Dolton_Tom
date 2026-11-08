package pe.edu.upeu.reniecmovil.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.reniecmovil.domain.model.Credenciales
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni
import pe.edu.upeu.reniecmovil.domain.repository.AuthRepository
import pe.edu.upeu.reniecmovil.domain.repository.TramiteRepository

class ListarTramitesUseCase(
    private val repository: TramiteRepository
) {
    operator fun invoke(): Flow<List<TramiteDni>> = repository.observarTramites()
}

class GuardarTramiteUseCase(
    private val repository: TramiteRepository
) {
    suspend operator fun invoke(
        id: Long = 0L,
        dniCiudadano: String,
        nombreCiudadano: String,
        tipoTramite: String,
        estado: String,
        ubigeoDestino: String,
        observaciones: String,
        fechaRegistro: String = "2026-11-04"
    ): Result<TramiteDni> = resultadoDe {
        val dniTrim = dniCiudadano.trim()
        val nombreTrim = nombreCiudadano.trim()
        val ubigeoTrim = ubigeoDestino.trim()
        val obsTrim = observaciones.trim()

        val errores = ErroresTramite(
            dniCiudadano = when {
                dniTrim.isBlank() -> "El DNI del ciudadano es obligatorio"
                dniTrim.length != 8 || !dniTrim.all { it.isDigit() } -> "El DNI debe tener 8 dígitos numéricos"
                else -> null
            },
            nombreCiudadano = if (nombreTrim.length < 3) "Ingrese el nombre completo del ciudadano" else null,
            ubigeoDestino = if (ubigeoTrim.length < 3) "Indique la sede o ubigeo del trámite" else null,
            observaciones = if (obsTrim.length < 4) "Ingrese el motivo o detalle de la solicitud" else null
        )

        if (errores.hayErrores) {
            throw TramiteInvalidoException(errores)
        }

        val tramite = TramiteDni(
            id = id,
            dniCiudadano = dniTrim,
            nombreCiudadano = nombreTrim.uppercase(),
            tipoTramite = tipoTramite,
            estado = estado,
            ubigeoDestino = ubigeoTrim,
            observaciones = obsTrim,
            fechaRegistro = fechaRegistro
        )

        if (id == 0L) {
            repository.registrar(tramite)
        } else {
            repository.actualizar(tramite)
        }
    }
}

class EliminarTramiteUseCase(
    private val repository: TramiteRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> = resultadoDe {
        repository.eliminar(id)
    }
}

class IniciarSesionUseCase(
    val authRepository: AuthRepository
) {
    suspend operator fun invoke(credenciales: Credenciales): Result<Sesion> = resultadoDe {
        authRepository.iniciarSesion(credenciales)
    }

    suspend fun enviarCodigoEmail(correo: String): Result<Unit> = resultadoDe {
        authRepository.enviarCodigoRecuperacionEmail(correo)
    }

    suspend fun verificarCodigoEmail(correo: String, codigoOtp: String): Result<Sesion> = resultadoDe {
        authRepository.verificarCodigoRecuperacionEmail(correo, codigoOtp)
    }
}

class ObtenerSesionGuardadaUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Sesion? = authRepository.sesionGuardada()
}

class CerrarSesionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.cerrarSesion()
}
