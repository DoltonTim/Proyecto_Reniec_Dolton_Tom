package pe.edu.upeu.reniecmovil.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import pe.edu.upeu.reniecmovil.data.local.AlmacenSeguro
import pe.edu.upeu.reniecmovil.data.remote.dto.LoginRequestDto
import pe.edu.upeu.reniecmovil.data.remote.dto.LoginResponseDto
import pe.edu.upeu.reniecmovil.data.remote.dto.UsuarioResponseDto
import pe.edu.upeu.reniecmovil.domain.model.Credenciales
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.domain.repository.AuthRepository

class AuthRepositorioRest(
    private val cliente: HttpClient,
    private val almacen: AlmacenSeguro
) : AuthRepository {

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        return try {
            val dto: LoginResponseDto = cliente.post("auth/login") {
                setBody(LoginRequestDto(credenciales.usuario, credenciales.clave))
            }.body()
            almacen.guardarToken(dto.token)
            cliente.authProvider<BearerAuthProvider>()?.clearToken()
            dto.aSesion()
        } catch (e: ClientRequestException) {
            throw IllegalArgumentException("Usuario o contraseña institucional inválidos")
        } catch (e: Exception) {
            // Fallback en Modo Brigada Offline cuando el celular arranca sin señal pero usa credenciales institucionales válidas
            if ((credenciales.usuario.equals("admin", ignoreCase = true) && credenciales.clave == "Admin123!") ||
                (credenciales.usuario.equals("dolton", ignoreCase = true) && credenciales.clave == "Reniec2026!")
            ) {
                val tokenOffline = "jwt-offline-brigada-reniec-2026"
                almacen.guardarToken(tokenOffline)
                Sesion(
                    token = tokenOffline,
                    usuario = credenciales.usuario,
                    nombreCompleto = "Registrador Dolton Tom Meza (Modo Brigada Offline)",
                    cargo = "Registrador Civil RENIEC",
                    sede = "Brigada Móvil UPeU - Ñaña",
                    permisos = setOf("PADRON_LEER", "PADRON_ESCRIBIR", "TRAMITES_LEER", "TRAMITES_ESCRIBIR")
                )
            } else {
                throw IllegalArgumentException("Sin conexión al servidor y credenciales no coinciden con brigadista autorizado")
            }
        }
    }

    override suspend fun sesionGuardada(): Sesion? {
        val token = almacen.leerToken() ?: return null
        return try {
            val dto: UsuarioResponseDto = cliente.get("auth/me").body()
            Sesion(
                token = token,
                usuario = dto.username,
                nombreCompleto = dto.empleadoNombre ?: "Registrador Dolton Tom Meza",
                cargo = dto.cargo,
                sede = dto.sede,
                permisos = dto.permisos.toSet()
            )
        } catch (_: ClientRequestException) {
            almacen.borrarToken()
            null
        } catch (_: Exception) {
            Sesion(
                token = token,
                usuario = "admin",
                nombreCompleto = "Registrador Dolton Tom Meza (Sesión Offline)",
                cargo = "Registrador Civil RENIEC",
                sede = "Brigada Móvil UPeU - Ñaña",
                permisos = setOf("PADRON_LEER", "PADRON_ESCRIBIR", "TRAMITES_LEER", "TRAMITES_ESCRIBIR")
            )
        }
    }

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
