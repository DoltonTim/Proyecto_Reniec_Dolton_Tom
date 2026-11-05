package pe.edu.upeu.reniecmovil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.reniecmovil.data.repository.PADRON_SEMILLA_RENIEC
import pe.edu.upeu.reniecmovil.data.repository.TRAMITES_SEMILLA_RENIEC
import pe.edu.upeu.reniecmovil.db.Ciudadano_local
import pe.edu.upeu.reniecmovil.db.ReniecDb
import pe.edu.upeu.reniecmovil.db.Tramite_local
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni

class ReniecLocalDataSource(
    base: ReniecDb,
    cargarSemillaSiVacio: Boolean = true
) {
    private val queries = base.reniecStorageQueries

    init {
        if (cargarSemillaSiVacio && queries.contarCiudadanos().executeAsOne() == 0L) {
            PADRON_SEMILLA_RENIEC.forEach { c ->
                insertarCiudadanoInternal(c, pendiente = false)
            }
        }
        if (cargarSemillaSiVacio && queries.contarTramites().executeAsOne() == 0L) {
            TRAMITES_SEMILLA_RENIEC.forEach { t ->
                insertarTramiteInternal(t, pendiente = false)
            }
        }
    }

    fun observarCiudadanos(): Flow<List<Ciudadano>> =
        queries.listarCiudadanos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.toDomain() } }

    fun buscarCiudadanoPorDni(dni: String): Ciudadano? =
        queries.buscarCiudadanoPorDni(dni).executeAsOneOrNull()?.toDomain()

    fun ciudadanosPendientes(): List<Ciudadano> =
        queries.listarCiudadanosPendientes().executeAsList().map { it.toDomain() }

    fun guardarCiudadanoPendiente(ciudadano: Ciudadano): Ciudadano {
        val idAsignado = if (ciudadano.id == 0L) {
            val menor = queries.siguienteIdCiudadanoLocal().executeAsOne()
            minOf(menor, 0L) - 1L
        } else {
            ciudadano.id
        }
        val local = ciudadano.copy(id = idAsignado, pendienteSync = true)
        insertarCiudadanoInternal(local, pendiente = true)
        return local
    }

    fun eliminarCiudadano(id: Long) {
        queries.eliminarCiudadanoPorId(id)
    }

    fun reemplazarCiudadanosSincronizados(remotos: List<Ciudadano>) {
        queries.transaction {
            queries.borrarCiudadanosSincronizados()
            remotos.forEach { ciudadano ->
                insertarCiudadanoInternal(ciudadano.copy(pendienteSync = false), pendiente = false)
            }
        }
    }

    fun marcarCiudadanoSincronizado(idLocalAnterior: Long, sincronizado: Ciudadano) {
        queries.transaction {
            queries.eliminarCiudadanoPorId(idLocalAnterior)
            insertarCiudadanoInternal(sincronizado.copy(pendienteSync = false), pendiente = false)
        }
    }

    fun observarTramites(): Flow<List<TramiteDni>> =
        queries.listarTramites()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.toDomain() } }

    fun tramitesPendientes(): List<TramiteDni> =
        queries.listarTramitesPendientes().executeAsList().map { it.toDomain() }

    fun guardarTramitePendiente(tramite: TramiteDni): TramiteDni {
        val idAsignado = if (tramite.id == 0L) {
            val menor = queries.siguienteIdTramiteLocal().executeAsOne()
            minOf(menor, 0L) - 1L
        } else {
            tramite.id
        }
        val local = tramite.copy(id = idAsignado, pendienteSync = true)
        insertarTramiteInternal(local, pendiente = true)
        return local
    }

    fun eliminarTramite(id: Long) {
        queries.eliminarTramitePorId(id)
    }

    fun reemplazarTramitesSincronizados(remotos: List<TramiteDni>) {
        queries.transaction {
            queries.borrarTramitesSincronizados()
            remotos.forEach { tramite ->
                insertarTramiteInternal(tramite.copy(pendienteSync = false), pendiente = false)
            }
        }
    }

    fun marcarTramiteSincronizado(idLocalAnterior: Long, sincronizado: TramiteDni) {
        queries.transaction {
            queries.eliminarTramitePorId(idLocalAnterior)
            insertarTramiteInternal(sincronizado.copy(pendienteSync = false), pendiente = false)
        }
    }

    private fun insertarCiudadanoInternal(c: Ciudadano, pendiente: Boolean) {
        queries.insertarCiudadano(
            id = c.id,
            dni = c.dni,
            nombres = c.nombres,
            apellidoPaterno = c.apellidoPaterno,
            apellidoMaterno = c.apellidoMaterno,
            fechaNacimiento = c.fechaNacimiento,
            estadoCivil = c.estadoCivil,
            ubigeo = c.ubigeo,
            direccion = c.direccion,
            donacionOrganos = if (c.donacionOrganos) 1L else 0L,
            pendiente = if (pendiente) 1L else 0L
        )
    }

    private fun insertarTramiteInternal(t: TramiteDni, pendiente: Boolean) {
        queries.insertarTramite(
            id = t.id,
            dniCiudadano = t.dniCiudadano,
            nombreCiudadano = t.nombreCiudadano,
            tipoTramite = t.tipoTramite,
            estado = t.estado,
            ubigeoDestino = t.ubigeoDestino,
            observaciones = t.observaciones,
            fechaRegistro = t.fechaRegistro,
            pendiente = if (pendiente) 1L else 0L
        )
    }
}

private fun Ciudadano_local.toDomain(): Ciudadano = Ciudadano(
    id = id,
    dni = dni,
    nombres = nombres,
    apellidoPaterno = apellidoPaterno,
    apellidoMaterno = apellidoMaterno,
    fechaNacimiento = fechaNacimiento,
    estadoCivil = estadoCivil,
    ubigeo = ubigeo,
    direccion = direccion,
    donacionOrganos = donacionOrganos != 0L,
    pendienteSync = pendiente != 0L
)

private fun Tramite_local.toDomain(): TramiteDni = TramiteDni(
    id = id,
    dniCiudadano = dniCiudadano,
    nombreCiudadano = nombreCiudadano,
    tipoTramite = tipoTramite,
    estado = estado,
    ubigeoDestino = ubigeoDestino,
    observaciones = observaciones,
    fechaRegistro = fechaRegistro,
    pendienteSync = pendiente != 0L
)
