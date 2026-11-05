package pe.edu.upeu.reniecmovil.data.remote.dto

import kotlinx.serialization.Serializable
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String = "Bearer",
    val expiraEnSegundos: Long = 10800,
    val username: String,
    val nombreCompleto: String = "Registrador Dolton Tom Meza",
    val cargo: String = "Registrador Civil RENIEC",
    val sede: String = "Brigada Móvil UPeU - Ñaña",
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
) {
    fun aSesion(): Sesion = Sesion(
        token = token,
        usuario = username,
        nombreCompleto = nombreCompleto,
        cargo = cargo,
        sede = sede,
        permisos = permisos.toSet()
    )
}

@Serializable
data class UsuarioResponseDto(
    val username: String,
    val empleadoNombre: String? = null,
    val cargo: String = "Registrador Civil RENIEC",
    val sede: String = "Brigada Móvil UPeU - Ñaña",
    val permisos: List<String> = emptyList()
)

@Serializable
data class CiudadanoDto(
    val id: Long = 0L,
    val dni: String,
    val nombres: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val fechaNacimiento: String,
    val estadoCivil: String,
    val ubigeo: String,
    val direccion: String,
    val donacionOrganos: Boolean = true
) {
    fun toDomain(pendienteSync: Boolean = false): Ciudadano = Ciudadano(
        id = id,
        dni = dni,
        nombres = nombres,
        apellidoPaterno = apellidoPaterno,
        apellidoMaterno = apellidoMaterno,
        fechaNacimiento = fechaNacimiento,
        estadoCivil = estadoCivil,
        ubigeo = ubigeo,
        direccion = direccion,
        donacionOrganos = donacionOrganos,
        pendienteSync = pendienteSync
    )
}

fun Ciudadano.toDto(): CiudadanoDto = CiudadanoDto(
    id = if (id > 0) id else 0L,
    dni = dni,
    nombres = nombres,
    apellidoPaterno = apellidoPaterno,
    apellidoMaterno = apellidoMaterno,
    fechaNacimiento = fechaNacimiento,
    estadoCivil = estadoCivil,
    ubigeo = ubigeo,
    direccion = direccion,
    donacionOrganos = donacionOrganos
)

@Serializable
data class TramiteDto(
    val id: Long = 0L,
    val dniCiudadano: String,
    val nombreCiudadano: String,
    val tipoTramite: String,
    val estado: String,
    val ubigeoDestino: String,
    val observaciones: String,
    val fechaRegistro: String = "2026-11-04"
) {
    fun toDomain(pendienteSync: Boolean = false): TramiteDni = TramiteDni(
        id = id,
        dniCiudadano = dniCiudadano,
        nombreCiudadano = nombreCiudadano,
        tipoTramite = tipoTramite,
        estado = estado,
        ubigeoDestino = ubigeoDestino,
        observaciones = observaciones,
        fechaRegistro = fechaRegistro,
        pendienteSync = pendienteSync
    )
}

fun TramiteDni.toDto(): TramiteDto = TramiteDto(
    id = if (id > 0) id else 0L,
    dniCiudadano = dniCiudadano,
    nombreCiudadano = nombreCiudadano,
    tipoTramite = tipoTramite,
    estado = estado,
    ubigeoDestino = ubigeoDestino,
    observaciones = observaciones,
    fechaRegistro = fechaRegistro
)
