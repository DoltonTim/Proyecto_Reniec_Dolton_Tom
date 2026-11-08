package pe.edu.upeu.reniecmovil.data.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.reniecmovil.data.local.ReniecLocalDataSource
import pe.edu.upeu.reniecmovil.data.remote.CiudadanoApi
import pe.edu.upeu.reniecmovil.data.remote.TramiteApi
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni
import pe.edu.upeu.reniecmovil.domain.repository.CiudadanoRepository
import pe.edu.upeu.reniecmovil.domain.repository.TramiteRepository

class CiudadanoRepositorioOffline(
    private val local: ReniecLocalDataSource,
    private val remoto: CiudadanoApi
) : CiudadanoRepository {

    override fun observarCiudadanos(): Flow<List<Ciudadano>> = local.observarCiudadanos()

    override suspend fun buscarPorDni(dni: String): Ciudadano? {
        val enLocal = local.buscarCiudadanoPorDni(dni)
        if (enLocal != null) return enLocal
        return try {
            val desdeApi = remoto.buscarPorDni(dni)
            if (desdeApi != null) {
                local.marcarCiudadanoSincronizado(desdeApi.id, desdeApi)
            }
            desdeApi
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun buscarEnPadronMasivo(consulta: String, departamento: String): List<Ciudadano> {
        val resultadosRemotos = remoto.buscarEnPadronMasivo(consulta, departamento)
        resultadosRemotos.take(30).forEach { ciudadano ->
            local.marcarCiudadanoSincronizado(ciudadano.id, ciudadano)
        }
        return resultadosRemotos
    }

    override suspend fun obtenerTotalPadron(): Long = remoto.obtenerTotalPadron()

    override suspend fun registrar(ciudadano: Ciudadano): Ciudadano {
        val guardadoLocal = local.guardarCiudadanoPendiente(ciudadano)
        intentarSinFallar { sincronizar() }
        return guardadoLocal
    }

    override suspend fun actualizar(ciudadano: Ciudadano): Ciudadano {
        val guardadoLocal = local.guardarCiudadanoPendiente(ciudadano)
        intentarSinFallar { sincronizar() }
        return guardadoLocal
    }

    override suspend fun eliminar(id: Long) {
        local.eliminarCiudadano(id)
        if (id > 0) {
            intentarSinFallar { remoto.eliminar(id) }
        }
    }

    override suspend fun sincronizar() {
        local.ciudadanosPendientes().forEach { pendiente ->
            val respuesta = if (pendiente.id < 0) {
                remoto.registrar(pendiente)
            } else {
                remoto.actualizar(pendiente)
            }
            local.marcarCiudadanoSincronizado(pendiente.id, respuesta)
        }
        val listaServidor = remoto.listar()
        local.reemplazarCiudadanosSincronizados(listaServidor)
    }

    private suspend fun intentarSinFallar(bloque: suspend () -> Unit) {
        try {
            bloque()
        } catch (_: Exception) {
            // Queda pendiente en SQLDelight para cuando regrese la señal
        }
    }
}

class TramiteRepositorioOffline(
    private val local: ReniecLocalDataSource,
    private val remoto: TramiteApi
) : TramiteRepository {

    override fun observarTramites(): Flow<List<TramiteDni>> = local.observarTramites()

    override suspend fun registrar(tramite: TramiteDni): TramiteDni {
        val guardadoLocal = local.guardarTramitePendiente(tramite)
        intentarSinFallar { sincronizar() }
        return guardadoLocal
    }

    override suspend fun actualizar(tramite: TramiteDni): TramiteDni {
        val guardadoLocal = local.guardarTramitePendiente(tramite)
        intentarSinFallar { sincronizar() }
        return guardadoLocal
    }

    override suspend fun eliminar(id: Long) {
        local.eliminarTramite(id)
        if (id > 0) {
            intentarSinFallar { remoto.eliminar(id) }
        }
    }

    override suspend fun sincronizar() {
        local.tramitesPendientes().forEach { pendiente ->
            val respuesta = if (pendiente.id < 0) {
                remoto.registrar(pendiente)
            } else {
                remoto.actualizar(pendiente)
            }
            local.marcarTramiteSincronizado(pendiente.id, respuesta)
        }
        val listaServidor = remoto.listar()
        local.reemplazarTramitesSincronizados(listaServidor)
    }

    override fun contarPendientesSync(): Int = local.tramitesPendientes().size

    private suspend fun intentarSinFallar(bloque: suspend () -> Unit) {
        try {
            bloque()
        } catch (_: Exception) {
            // Persiste como pendiente en SQLDelight hasta recuperar señal
        }
    }
}
