package pe.edu.upeu.reniecmovil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.Credenciales
import pe.edu.upeu.reniecmovil.domain.model.EstadoCivil
import pe.edu.upeu.reniecmovil.domain.model.EstadoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.domain.model.TipoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni
import pe.edu.upeu.reniecmovil.domain.repository.AuthRepository
import pe.edu.upeu.reniecmovil.domain.repository.CiudadanoRepository
import pe.edu.upeu.reniecmovil.domain.repository.TramiteRepository

val PADRON_SEMILLA_RENIEC = listOf(
    Ciudadano(
        id = 1L,
        dni = "72345678",
        nombres = "DOLTON TOM",
        apellidoPaterno = "MEZA",
        apellidoMaterno = "QUISPE",
        fechaNacimiento = "2001-05-14",
        estadoCivil = EstadoCivil.SOLTERO.name,
        ubigeo = "LIMA / LIMA / LURIGANCHO-CHOSICA",
        direccion = "CARRETERA CENTRAL KM 19.5 VILLA UNION - ÑAÑA",
        donacionOrganos = true
    ),
    Ciudadano(
        id = 2L,
        dni = "45896321",
        nombres = "MARIA ELENA",
        apellidoPaterno = "PAREDES",
        apellidoMaterno = "MENDOZA",
        fechaNacimiento = "1995-08-22",
        estadoCivil = EstadoCivil.CASADO.name,
        ubigeo = "JUNIN / HUANCAYO / EL TAMBO",
        direccion = "AV. REAL 1240 URB. SAN CARLOS",
        donacionOrganos = true
    ),
    Ciudadano(
        id = 3L,
        dni = "70124589",
        nombres = "JORGE LUIS",
        apellidoPaterno = "HUAMAN",
        apellidoMaterno = "ROJAS",
        fechaNacimiento = "1998-11-03",
        estadoCivil = EstadoCivil.SOLTERO.name,
        ubigeo = "PUNO / SAN ROMAN / JULIACA",
        direccion = "JR. MARIANO NUÑEZ 450",
        donacionOrganos = false
    ),
    Ciudadano(
        id = 4L,
        dni = "41258796",
        nombres = "ROSA VICTORIA",
        apellidoPaterno = "CASTILLO",
        apellidoMaterno = "VARGAS",
        fechaNacimiento = "1989-02-19",
        estadoCivil = EstadoCivil.SOLTERO.name,
        ubigeo = "SAN MARTIN / SAN MARTIN / TARAPOTO",
        direccion = "JR. JIMENEZ PIMENTEL 890",
        donacionOrganos = true
    ),
    Ciudadano(
        id = 5L,
        dni = "76543210",
        nombres = "CARLOS ALBERTO",
        apellidoPaterno = "SANCHEZ",
        apellidoMaterno = "GAMARRA",
        fechaNacimiento = "2003-07-30",
        estadoCivil = EstadoCivil.SOLTERO.name,
        ubigeo = "LIMA / LIMA / ATE",
        direccion = "AV. NICOLAS AYLLON 3420 SANTA CLARA",
        donacionOrganos = true
    )
)

val TRAMITES_SEMILLA_RENIEC = listOf(
    TramiteDni(
        id = 1L,
        dniCiudadano = "72345678",
        nombreCiudadano = "DOLTON TOM MEZA QUISPE",
        tipoTramite = TipoTramiteDni.RENOVACION.name,
        estado = EstadoTramiteDni.ATENDIDO.name,
        ubigeoDestino = "LIMA / LURIGANCHO - AGENCIA ÑAÑA",
        observaciones = "Renovación a DNI Electrónico 3.0 con firma digital activa",
        fechaRegistro = "2026-11-01"
    ),
    TramiteDni(
        id = 2L,
        dniCiudadano = "45896321",
        nombreCiudadano = "MARIA ELENA PAREDES MENDOZA",
        tipoTramite = TipoTramiteDni.CAMBIO_DOMICILIO.name,
        estado = EstadoTramiteDni.EN_PROCESO.name,
        ubigeoDestino = "JUNIN / HUANCAYO",
        observaciones = "Actualización de dirección domiciliaria con recibo de servicio verificado",
        fechaRegistro = "2026-11-02"
    ),
    TramiteDni(
        id = 3L,
        dniCiudadano = "70124589",
        nombreCiudadano = "JORGE LUIS HUAMAN ROJAS",
        tipoTramite = TipoTramiteDni.DUPLICADO.name,
        estado = EstadoTramiteDni.PENDIENTE.name,
        ubigeoDestino = "PUNO / JULIACA",
        observaciones = "Solicitud de duplicado por pérdida en campaña itinerante",
        fechaRegistro = "2026-11-03"
    )
)

class CiudadanoRepositorioEnMemoria(
    iniciales: List<Ciudadano> = PADRON_SEMILLA_RENIEC
) : CiudadanoRepository {

    private val estado = MutableStateFlow(iniciales)

    override fun observarCiudadanos(): Flow<List<Ciudadano>> = estado.asStateFlow()

    override suspend fun buscarPorDni(dni: String): Ciudadano? =
        estado.value.firstOrNull { it.dni == dni.trim() }

    override suspend fun registrar(ciudadano: Ciudadano): Ciudadano {
        val nuevoId = (estado.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val creado = ciudadano.copy(id = nuevoId)
        estado.update { listOf(creado) + it }
        return creado
    }

    override suspend fun actualizar(ciudadano: Ciudadano): Ciudadano {
        estado.update { lista ->
            lista.map { if (it.id == ciudadano.id) ciudadano else it }
        }
        return ciudadano
    }

    override suspend fun eliminar(id: Long) {
        estado.update { lista -> lista.filterNot { it.id == id } }
    }

    override suspend fun sincronizar() = Unit
}

class TramiteRepositorioEnMemoria(
    iniciales: List<TramiteDni> = TRAMITES_SEMILLA_RENIEC
) : TramiteRepository {

    private val estado = MutableStateFlow(iniciales)

    override fun observarTramites(): Flow<List<TramiteDni>> = estado.asStateFlow()

    override suspend fun registrar(tramite: TramiteDni): TramiteDni {
        val nuevoId = (estado.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val creado = tramite.copy(id = nuevoId)
        estado.update { listOf(creado) + it }
        return creado
    }

    override suspend fun actualizar(tramite: TramiteDni): TramiteDni {
        estado.update { lista ->
            lista.map { if (it.id == tramite.id) tramite else it }
        }
        return tramite
    }

    override suspend fun eliminar(id: Long) {
        estado.update { lista -> lista.filterNot { it.id == id } }
    }

    override suspend fun sincronizar() = Unit

    override fun contarPendientesSync(): Int = estado.value.count { it.pendienteSync }
}

class AuthRepositorioEnMemoria : AuthRepository {
    private var sesionActual: Sesion? = null

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        if (credenciales.usuario.isBlank() || credenciales.clave.length < 4) {
            throw IllegalArgumentException("Credenciales inválidas para Registrador RENIEC")
        }
        val sesion = Sesion(
            token = "jwt-demo-token-reniec-2026",
            usuario = credenciales.usuario,
            nombreCompleto = "Registrador Dolton Tom Meza",
            cargo = "Registrador Civil RENIEC",
            sede = "Brigada Móvil UPeU - Ñaña",
            permisos = setOf("PADRON_LEER", "PADRON_ESCRIBIR", "TRAMITES_LEER", "TRAMITES_ESCRIBIR")
        )
        sesionActual = sesion
        return sesion
    }

    override suspend fun sesionGuardada(): Sesion? = sesionActual

    override suspend fun cerrarSesion() {
        sesionActual = null
    }

    override suspend fun enviarCodigoRecuperacionEmail(correo: String) = Unit

    override suspend fun verificarCodigoRecuperacionEmail(correo: String, codigoOtp: String): Sesion {
        val sesion = Sesion(
            token = "jwt-demo-otp-reniec-2026",
            usuario = "dolton",
            nombreCompleto = "Registrador Dolton Tom Meza ($correo)",
            cargo = "Registrador Civil RENIEC",
            sede = "Brigada Móvil UPeU - Ñaña",
            permisos = setOf("PADRON_LEER", "PADRON_ESCRIBIR", "TRAMITES_LEER", "TRAMITES_ESCRIBIR")
        )
        sesionActual = sesion
        return sesion
    }
}
