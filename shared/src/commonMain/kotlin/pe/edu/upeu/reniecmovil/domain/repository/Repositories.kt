package pe.edu.upeu.reniecmovil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.Credenciales
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni

interface CiudadanoRepository {
    fun observarCiudadanos(): Flow<List<Ciudadano>>
    suspend fun buscarPorDni(dni: String): Ciudadano?
    suspend fun registrar(ciudadano: Ciudadano): Ciudadano
    suspend fun actualizar(ciudadano: Ciudadano): Ciudadano
    suspend fun eliminar(id: Long)
    suspend fun sincronizar()
}

interface TramiteRepository {
    fun observarTramites(): Flow<List<TramiteDni>>
    suspend fun registrar(tramite: TramiteDni): TramiteDni
    suspend fun actualizar(tramite: TramiteDni): TramiteDni
    suspend fun eliminar(id: Long)
    suspend fun sincronizar()
    fun contarPendientesSync(): Int
}

interface AuthRepository {
    suspend fun iniciarSesion(credenciales: Credenciales): Sesion
    suspend fun sesionGuardada(): Sesion?
    suspend fun cerrarSesion()
    suspend fun enviarCodigoRecuperacionEmail(correo: String)
    suspend fun verificarCodigoRecuperacionEmail(correo: String, codigoOtp: String): Sesion
}
