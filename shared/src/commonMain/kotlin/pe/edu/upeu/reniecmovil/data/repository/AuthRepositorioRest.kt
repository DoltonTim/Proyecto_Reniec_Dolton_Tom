package pe.edu.upeu.reniecmovil.data.repository

import io.ktor.client.HttpClient
import pe.edu.upeu.reniecmovil.data.local.AlmacenSeguro
import pe.edu.upeu.reniecmovil.domain.model.Credenciales
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.domain.repository.AuthRepository

class AuthRepositorioRest(
    @Suppress("UNUSED_PARAMETER") private val cliente: HttpClient,
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
                cargo = "Verificador Biométrico AFIS y Ventanilla",
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
                cargo = "Verificador Biométrico AFIS y Ventanilla",
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
