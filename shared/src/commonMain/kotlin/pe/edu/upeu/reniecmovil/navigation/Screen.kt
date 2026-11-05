package pe.edu.upeu.reniecmovil.navigation

sealed class Screen(val route: String) {
    data object Inicio : Screen("inicio")
    data object PadronDni : Screen("padron_dni")
    data object Tramites : Screen("tramites")
    data object Perfil : Screen("perfil")
}
