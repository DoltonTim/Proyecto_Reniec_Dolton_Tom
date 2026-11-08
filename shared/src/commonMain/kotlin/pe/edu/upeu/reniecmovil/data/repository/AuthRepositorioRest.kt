package pe.edu.upeu.reniecmovil.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import pe.edu.upeu.reniecmovil.data.local.AlmacenSeguro
import pe.edu.upeu.reniecmovil.domain.model.Credenciales
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.domain.repository.AuthRepository

@Serializable
private data class SupabaseOtpRequest(
    val email: String,
    val create_user: Boolean = true
)

@Serializable
private data class SupabaseVerifyOtpRequest(
    val email: String,
    val token: String,
    val type: String = "email"
)

class AuthRepositorioRest(
    private val cliente: HttpClient,
    private val almacen: AlmacenSeguro
) : AuthRepository {

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        val u = credenciales.usuario.trim().lowercase().substringBefore("@")
        val c = credenciales.clave.trim()

        val sesion = when {
            u == "admin" && (c.equals("Admin123!", ignoreCase = true) || c.equals("Admin123", ignoreCase = true)) -> Sesion(
                token = "jwt-supabase-admin-2026",
                usuario = "admin",
                nombreCompleto = "Ing. Carlos Mendoza (Administrador General)",
                cargo = "Jefe de Registro Civil y Supervisión",
                sede = "Sede Central RENIEC - Lima",
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
            u == "dolton" && (c.equals("Reniec2026!", ignoreCase = true) || c.equals("Reniec2026", ignoreCase = true)) -> Sesion(
                token = "jwt-supabase-brigadista-2026",
                usuario = "dolton",
                nombreCompleto = "Brigadista Dolton Tom Meza Quispe",
                cargo = "Registrador de Brigada Itinerante",
                sede = "Brigada Móvil UPeU - Lurigancho Ñaña",
                permisos = setOf(
                    "ROLE_BRIGADISTA",
                    "PADRON_LEER",
                    "PADRON_ESCRIBIR",
                    "TRAMITES_LEER",
                    "TRAMITES_ESCRIBIR"
                )
            )
            u == "verificador" && (c.equals("Verifica2026!", ignoreCase = true) || c.equals("Verifica2026", ignoreCase = true)) -> Sesion(
                token = "jwt-supabase-verificador-2026",
                usuario = "verificador",
                nombreCompleto = "Lic. Ana Torres (Módulo Consulta)",
                cargo = "Verificador de Padrón y Ventanilla",
                sede = "Agencia RENIEC Ate - Vitarte",
                permisos = setOf(
                    "ROLE_VERIFICADOR",
                    "PADRON_LEER",
                    "TRAMITES_LEER"
                )
            )
            else -> null
        } ?: throw IllegalArgumentException("Usuario o contraseña institucional inválidos")

        almacen.guardarToken("${sesion.usuario}|${sesion.token}")
        return sesion
    }

    override suspend fun enviarCodigoRecuperacionEmail(correo: String) {
        val limpio = correo.trim()
        require(limpio.contains("@") && limpio.contains(".")) {
            "Ingresa un correo electrónico válido (ej: usuario@gmail.com)"
        }
        cliente.post("https://vktclzpkebwvfbrvtpxo.supabase.co/auth/v1/otp") {
            setBody(SupabaseOtpRequest(email = limpio, create_user = true))
        }
    }

    override suspend fun verificarCodigoRecuperacionEmail(correo: String, codigoOtp: String): Sesion {
        val limpio = correo.trim()
        val otp = codigoOtp.trim()
        require(otp.length >= 6) {
            "Ingresa el código de 6 dígitos que llegó a tu correo"
        }
        try {
            cliente.post("https://vktclzpkebwvfbrvtpxo.supabase.co/auth/v1/verify") {
                setBody(SupabaseVerifyOtpRequest(email = limpio, token = otp, type = "email"))
            }
        } catch (e: Exception) {
            try {
                cliente.post("https://vktclzpkebwvfbrvtpxo.supabase.co/auth/v1/verify") {
                    setBody(SupabaseVerifyOtpRequest(email = limpio, token = otp, type = "magiclink"))
                }
            } catch (_: Exception) {
                throw IllegalArgumentException("El código ingresado es incorrecto o ha expirado")
            }
        }

        val sesion = Sesion(
            token = "jwt-supabase-google-otp-2026",
            usuario = "dolton",
            nombreCompleto = "Brigadista Dolton Tom Meza Quispe (${limpio})",
            cargo = "Registrador de Brigada Itinerante",
            sede = "Brigada Móvil UPeU - Lurigancho Ñaña",
            permisos = setOf(
                "ROLE_BRIGADISTA",
                "PADRON_LEER",
                "PADRON_ESCRIBIR",
                "TRAMITES_LEER",
                "TRAMITES_ESCRIBIR"
            )
        )
        almacen.guardarToken("${sesion.usuario}|${sesion.token}")
        return sesion
    }

    override suspend fun sesionGuardada(): Sesion? {
        val raw = almacen.leerToken() ?: return null
        val partes = raw.split("|", limit = 2)
        val usuarioGuardado = if (partes.size == 2) partes[0] else "admin"
        val tokenPuro = if (partes.size == 2) partes[1] else raw

        return when (usuarioGuardado.lowercase()) {
            "dolton" -> Sesion(
                token = tokenPuro,
                usuario = "dolton",
                nombreCompleto = "Brigadista Dolton Tom Meza Quispe",
                cargo = "Registrador de Brigada Itinerante",
                sede = "Brigada Móvil UPeU - Lurigancho Ñaña",
                permisos = setOf(
                    "ROLE_BRIGADISTA",
                    "PADRON_LEER",
                    "PADRON_ESCRIBIR",
                    "TRAMITES_LEER",
                    "TRAMITES_ESCRIBIR"
                )
            )
            "verificador" -> Sesion(
                token = tokenPuro,
                usuario = "verificador",
                nombreCompleto = "Lic. Ana Torres (Módulo Consulta)",
                cargo = "Verificador de Padrón y Ventanilla",
                sede = "Agencia RENIEC Ate - Vitarte",
                permisos = setOf(
                    "ROLE_VERIFICADOR",
                    "PADRON_LEER",
                    "TRAMITES_LEER"
                )
            )
            else -> Sesion(
                token = tokenPuro,
                usuario = "admin",
                nombreCompleto = "Ing. Carlos Mendoza (Administrador General)",
                cargo = "Jefe de Registro Civil y Supervisión",
                sede = "Sede Central RENIEC - Lima",
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

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
    }
}
