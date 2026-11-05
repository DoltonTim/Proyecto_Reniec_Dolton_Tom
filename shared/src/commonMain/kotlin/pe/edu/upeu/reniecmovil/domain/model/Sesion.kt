package pe.edu.upeu.reniecmovil.domain.model

data class Credenciales(
    val usuario: String,
    val clave: String
)

data class Sesion(
    val token: String,
    val usuario: String,
    val nombreCompleto: String,
    val cargo: String = "Registrador Civil RENIEC",
    val sede: String = "Brigada Móvil Lima - Ñaña",
    val permisos: Set<String> = emptySet()
) {
    fun puede(permiso: String): Boolean = permisos.contains(permiso)
}

sealed interface ErrorSesion {
    data object CredencialesInvalidas : ErrorSesion
    data object SesionExpirada : ErrorSesion
    data object SinPermiso : ErrorSesion
    data object SinConexion : ErrorSesion
}
