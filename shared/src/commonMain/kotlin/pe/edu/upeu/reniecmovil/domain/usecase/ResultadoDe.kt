package pe.edu.upeu.reniecmovil.domain.usecase

import kotlinx.coroutines.CancellationException

inline fun <T> resultadoDe(bloque: () -> T): Result<T> = try {
    Result.success(bloque())
} catch (cancelacion: CancellationException) {
    throw cancelacion
} catch (e: Throwable) {
    Result.failure(e)
}

data class ErroresCiudadano(
    val dni: String? = null,
    val nombres: String? = null,
    val apellidoPaterno: String? = null,
    val apellidoMaterno: String? = null,
    val fechaNacimiento: String? = null,
    val ubigeo: String? = null,
    val direccion: String? = null
) {
    val hayErrores: Boolean
        get() = listOfNotNull(dni, nombres, apellidoPaterno, apellidoMaterno, fechaNacimiento, ubigeo, direccion).isNotEmpty()
}

class CiudadanoInvalidoException(val errores: ErroresCiudadano) : IllegalArgumentException("Datos del ciudadano inválidos")

data class ErroresTramite(
    val dniCiudadano: String? = null,
    val nombreCiudadano: String? = null,
    val ubigeoDestino: String? = null,
    val observaciones: String? = null
) {
    val hayErrores: Boolean
        get() = listOfNotNull(dniCiudadano, nombreCiudadano, ubigeoDestino, observaciones).isNotEmpty()
}

class TramiteInvalidoException(val errores: ErroresTramite) : IllegalArgumentException("Datos del trámite DNI inválidos")
