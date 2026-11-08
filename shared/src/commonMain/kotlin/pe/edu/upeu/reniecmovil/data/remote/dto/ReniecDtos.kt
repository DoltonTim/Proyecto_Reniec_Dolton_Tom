package pe.edu.upeu.reniecmovil.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pe.edu.upeu.reniecmovil.BiometriaSesionCache
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
    @SerialName("apellido_paterno")
    val apellidoPaterno: String,
    @SerialName("apellido_materno")
    val apellidoMaterno: String,
    @SerialName("fecha_nacimiento")
    val fechaNacimiento: String,
    @SerialName("estado_civil")
    val estadoCivil: String,
    val ubigeo: String,
    val direccion: String,
    @SerialName("donacion_organos")
    val donacionOrganos: Boolean = true,
    @SerialName("biometria_facial")
    val biometriaFacial: String = "REGISTRADO_AFIS_99.6%",
    @SerialName("biometria_huella")
    val biometriaHuella: String = "HUELLA_INDICE_DER_OK"
) {
    fun toDomain(pendienteSync: Boolean = false): Ciudadano {
        if (biometriaFacial.isNotBlank() && !BiometriaSesionCache.estadoFacialPorDni.containsKey(dni)) {
            BiometriaSesionCache.estadoFacialPorDni[dni] = biometriaFacial
        }
        if (biometriaHuella.isNotBlank() && !BiometriaSesionCache.estadoHuellaPorDni.containsKey(dni)) {
            BiometriaSesionCache.estadoHuellaPorDni[dni] = biometriaHuella
        }
        return Ciudadano(
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
}

@Serializable
data class CiudadanoPayloadDto(
    val dni: String,
    val nombres: String,
    @SerialName("apellido_paterno")
    val apellidoPaterno: String,
    @SerialName("apellido_materno")
    val apellidoMaterno: String,
    @SerialName("fecha_nacimiento")
    val fechaNacimiento: String,
    @SerialName("estado_civil")
    val estadoCivil: String,
    val ubigeo: String,
    val direccion: String,
    @SerialName("donacion_organos")
    val donacionOrganos: Boolean = true,
    @SerialName("biometria_facial")
    val biometriaFacial: String = "REGISTRADO_AFIS_99.6%",
    @SerialName("biometria_huella")
    val biometriaHuella: String = "HUELLA_INDICE_DER_OK"
)

fun Ciudadano.toPayloadDto(): CiudadanoPayloadDto = CiudadanoPayloadDto(
    dni = dni,
    nombres = nombres,
    apellidoPaterno = apellidoPaterno,
    apellidoMaterno = apellidoMaterno,
    fechaNacimiento = fechaNacimiento,
    estadoCivil = estadoCivil,
    ubigeo = ubigeo,
    direccion = direccion,
    donacionOrganos = donacionOrganos,
    biometriaFacial = BiometriaSesionCache.estadoFacialPorDni[dni] ?: "REGISTRADO_AFIS_99.6%",
    biometriaHuella = BiometriaSesionCache.estadoHuellaPorDni[dni] ?: "HUELLA_INDICE_DER_OK"
)

@Serializable
data class TramiteDto(
    val id: Long = 0L,
    @SerialName("dni_ciudadano")
    val dniCiudadano: String,
    @SerialName("nombre_ciudadano")
    val nombreCiudadano: String,
    @SerialName("tipo_tramite")
    val tipoTramite: String,
    val estado: String,
    @SerialName("ubigeo_destino")
    val ubigeoDestino: String,
    val observaciones: String,
    @SerialName("fecha_registro")
    val fechaRegistro: String = "2026-11-05"
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

@Serializable
data class TramitePayloadDto(
    @SerialName("dni_ciudadano")
    val dniCiudadano: String,
    @SerialName("nombre_ciudadano")
    val nombreCiudadano: String,
    @SerialName("tipo_tramite")
    val tipoTramite: String,
    val estado: String,
    @SerialName("ubigeo_destino")
    val ubigeoDestino: String,
    val observaciones: String,
    @SerialName("fecha_registro")
    val fechaRegistro: String = "2026-11-05"
)

fun TramiteDni.toPayloadDto(): TramitePayloadDto = TramitePayloadDto(
    dniCiudadano = dniCiudadano,
    nombreCiudadano = nombreCiudadano,
    tipoTramite = tipoTramite,
    estado = estado,
    ubigeoDestino = ubigeoDestino,
    observaciones = observaciones,
    fechaRegistro = fechaRegistro
)

@Serializable
data class AuditoriaDto(
    val id: Long = 0L,
    val usuario: String,
    val rol: String,
    val accion: String,
    val entidad: String,
    @SerialName("referencia_dni")
    val referenciaDni: String,
    val detalle: String,
    @SerialName("canal_telemetria")
    val canalTelemetria: String = "Google Analytics + Clarity",
    val dispositivo: String = "Android KMP",
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class AuditoriaPayloadDto(
    val usuario: String,
    val rol: String,
    val accion: String,
    val entidad: String,
    @SerialName("referencia_dni")
    val referenciaDni: String,
    val detalle: String,
    @SerialName("canal_telemetria")
    val canalTelemetria: String = "Google Analytics + Clarity",
    val dispositivo: String = "Android KMP"
)

enum class CargoOperadorReniec(
    val codigoRol: String,
    val etiquetaCargo: String,
    val descripcion: String,
    val permisosCsv: String
) {
    ADMINISTRADOR(
        codigoRol = "ROLE_ADMIN",
        etiquetaCargo = "Administrador General RENIEC",
        descripcion = "Control total: Padrón, Trámites, Aprobación, Eliminación y Gestión de Cargos",
        permisosCsv = "ROLE_ADMIN,PADRON_LEER,PADRON_ESCRIBIR,PADRON_ELIMINAR,TRAMITES_LEER,TRAMITES_ESCRIBIR,TRAMITES_APROBAR,TRAMITES_ELIMINAR,RECAUDACION_VER"
    ),
    EMPADRONADOR(
        codigoRol = "ROLE_EMPADRONADOR",
        etiquetaCargo = "Empadronador de Padrón y Biometría",
        descripcion = "Encargado de empadronar, editar ciudadanos y capturar Rostro/Huella",
        permisosCsv = "ROLE_EMPADRONADOR,PADRON_LEER,PADRON_ESCRIBIR,TRAMITES_LEER"
    ),
    TRAMITADOR(
        codigoRol = "ROLE_TRAMITES",
        etiquetaCargo = "Registrador y Gestor de Trámites DNI",
        descripcion = "Encargado de crear, actualizar, aprobar trámites y emitir Vouchers QR",
        permisosCsv = "ROLE_TRAMITES,PADRON_LEER,TRAMITES_LEER,TRAMITES_ESCRIBIR,TRAMITES_APROBAR"
    ),
    BRIGADISTA(
        codigoRol = "ROLE_BRIGADISTA",
        etiquetaCargo = "Brigadista Integral (Padrón y Trámites)",
        descripcion = "Registra ciudadanos y trámites en campañas itinerantes (sin borrado)",
        permisosCsv = "ROLE_BRIGADISTA,PADRON_LEER,PADRON_ESCRIBIR,TRAMITES_LEER,TRAMITES_ESCRIBIR"
    ),
    VERIFICADOR(
        codigoRol = "ROLE_VERIFICADOR",
        etiquetaCargo = "Verificador de Consulta y Ventanilla",
        descripcion = "Solo lectura de Padrón Nacional (+50K) y consulta de estado de trámites",
        permisosCsv = "ROLE_VERIFICADOR,PADRON_LEER,TRAMITES_LEER"
    );
}

@Serializable
data class OperadorReniecDto(
    val id: Long = 0L,
    val username: String,
    val password: String = "",
    @SerialName("nombre_completo")
    val nombreCompleto: String,
    val cargo: String,
    val rol: String,
    val sede: String = "Brigada Móvil UPeU - Lurigancho Ñaña",
    val permisos: String,
    val activo: Boolean = true
) {
    fun aSesion(): Sesion = Sesion(
        token = "jwt-supabase-${username.lowercase()}-2026",
        usuario = username.lowercase(),
        nombreCompleto = nombreCompleto,
        cargo = cargo,
        sede = sede,
        permisos = permisos.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    )
}

@Serializable
data class OperadorReniecPayloadDto(
    val username: String,
    val password: String,
    @SerialName("nombre_completo")
    val nombreCompleto: String,
    val cargo: String,
    val rol: String,
    val sede: String,
    val permisos: String,
    val activo: Boolean = true
)
