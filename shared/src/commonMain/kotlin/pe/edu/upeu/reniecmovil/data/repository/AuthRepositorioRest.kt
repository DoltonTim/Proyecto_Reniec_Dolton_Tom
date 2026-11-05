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
            almacen.guardarToken("${dto.username}|${dto.token}")
            cliente.authProvider<BearerAuthProvider>()?.clearToken()
            dto.aSesion()
        } catch (e: ClientRequestException) {
            throw IllegalArgumentException("Usuario o contraseña institucional inválidos")
        } catch (e: Exception) {
            // Fallback Offline-First para cualquiera de los 3 roles institucionales
            val u = credenciales.usuario.trim().lowercase()
            val c = credenciales.clave.trim()
            val sesionOffline = when {
                u == "admin" && c == "Admin123!" -> Sesion(
                    token = "jwt-offline-admin-2026",
                    usuario = "admin",
                    nombreCompleto = "Ing. Carlos Mendoza (Administrador RENIEC)",
                    cargo = "Jefe de Registro Civil - Control Total",
                    sede = "Sede Central RENIEC Lima",
                    permisos = setOf(
                        "ROLE_ADMIN",
                        "PADRON_LEER",
                        "PADRON_ESCRIBIR",
                        "PADRON_ELIMINAR",
                        "TRAMITES_LEER",
                        "TRAMITES_ESCRIBIR",
                        "TRAMITES_APROBAR",
                        "TRAMITES_ELIMINAR",
                        "RECAUDACION_VER"
                    )
                )
                u == "dolton" && c == "Reniec2026!" -> Sesion(
                    token = "jwt-offline-brigadista-2026",
                    usuario = "dolton",
                    nombreCompleto = "Brigadista Dolton Tom Meza",
                    cargo = "Brigadista de Campo (Offline/Online)",
                    sede = "Brigada Móvil UPeU - Ñaña",
                    permisos = setOf(
                        "ROLE_BRIGADISTA",
                        "PADRON_LEER",
                        "PADRON_ESCRIBIR",
                        "TRAMITES_LEER",
                        "TRAMITES_ESCRIBIR"
                    )
                )
                u == "verificador" && c == "Verifica2026!" -> Sesion(
                    token = "jwt-offline-verificador-2026",
                    usuario = "verificador",
                    nombreCompleto = "Lic. Ana Torres (Módulo Consulta)",
                    cargo = "Verificador Biométrico AFIS y Ventanilla",
                    sede = "Agencia RENIEC Ate - Vitarte",
                    permisos = setOf(
                        "ROLE_VERIFICADOR",
                        "PADRON_LEER",
                        "TRAMITES_LEER"
                    )
                )
                else -> null
            } ?: throw IllegalArgumentException("Credenciales no autorizadas para acceso RENIEC")

            almacen.guardarToken("${sesionOffline.usuario}|${sesionOffline.token}")
            sesionOffline
        }
    }

    override suspend fun sesionGuardada(): Sesion? {
        val raw = almacen.leerToken() ?: return null
        val partes = raw.split("|", limit = 2)
        val usuarioGuardado = if (partes.size == 2) partes[0] else "admin"
        val tokenPuro = if (partes.size == 2) partes[1] else raw

        return try {
            val dto: UsuarioResponseDto = cliente.get("auth/me").body()
            Sesion(
                token = tokenPuro,
                usuario = dto.username,
                nombreCompleto = dto.empleadoNombre ?: "Registrador RENIEC",
                cargo = dto.cargo,
                sede = dto.sede,
                permisos = dto.permisos.toSet()
            )
        } catch (_: ClientRequestException) {
            almacen.borrarToken()
            null
        } catch (_: Exception) {
            when (usuarioGuardado.lowercase()) {
                "dolton" -> Sesion(
                    token = tokenPuro,
                    usuario = "dolton",
                    nombreCompleto = "Brigadista Dolton Tom Meza (Offline)",
                    cargo = "Brigadista de Campo (Offline/Online)",
                    sede = "Brigada Móvil UPeU - Ñaña",
                    permisos = setOf("ROLE_BRIGADISTA", "PADRON_LEER", "PADRON_ESCRIBIR", "TRAMITES_LEER", "TRAMITES_ESCRIBIR")
                )
                "verificador" -> Sesion(
                    token = tokenPuro,
                    usuario = "verificador",
                    nombreCompleto = "Lic. Ana Torres (Módulo Consulta)",
                    cargo = "Verificador Biométrico AFIS y Ventanilla",
                    sede = "Agencia RENIEC Ate - Vitarte",
                    permisos = setOf("ROLE_VERIFICADOR", "PADRON_LEER", "TRAMITES_LEER")
                )
                else -> Sesion(
                    token = tokenPuro,
                    usuario = "admin",
                    nombreCompleto = "Ing. Carlos Mendoza (Admin Offline)",
                    cargo = "Jefe de Registro Civil - Control Total",
                    sede = "Sede Central RENIEC Lima",
                    permisos = setOf(
                        "ROLE_ADMIN",
                        "PADRON_LEER",
                        "PADRON_ESCRIBIR",
                        "PADRON_ELIMINAR",
                        "TRAMITES_LEER",
                        "TRAMITES_ESCRIBIR",
                        "TRAMITES_APROBAR",
                        "TRAMITES_ELIMINAR",
                        "RECAUDACION_VER"
                    )
                )
            }
        }
    }

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
