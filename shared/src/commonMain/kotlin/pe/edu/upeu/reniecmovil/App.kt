package pe.edu.upeu.reniecmovil

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.navigation.Screen
import pe.edu.upeu.reniecmovil.presentation.ciudadano.CiudadanoScreen
import pe.edu.upeu.reniecmovil.presentation.ciudadano.CiudadanoViewModel
import pe.edu.upeu.reniecmovil.presentation.inicio.InicioScreen
import pe.edu.upeu.reniecmovil.presentation.perfil.PerfilScreen
import pe.edu.upeu.reniecmovil.presentation.sesion.LoginScreen
import pe.edu.upeu.reniecmovil.presentation.sesion.SesionViewModel
import pe.edu.upeu.reniecmovil.presentation.tramite.TramiteScreen
import pe.edu.upeu.reniecmovil.presentation.tramite.TramiteViewModel
import pe.edu.upeu.reniecmovil.theme.ReniecMovilTheme

private data class DestinoNav(
    val screen: Screen,
    val titulo: String,
    val icono: ImageVector
)

@Composable
fun App() = KoinContext {
    val sesionViewModel: SesionViewModel = koinViewModel()
    val sesionState by sesionViewModel.estado.collectAsState()
    var darkTheme by rememberSaveable { mutableStateOf(false) }

    ReniecMovilTheme(darkTheme = darkTheme) {
        val sesion = sesionState.sesion
        if (sesion == null) {
            LoginScreen(viewModel = sesionViewModel)
        } else {
            ReniecMainScaffold(
                sesion = sesion,
                darkTheme = darkTheme,
                onCambiarTema = { darkTheme = it },
                onCerrarSesion = { sesionViewModel.cerrarSesion() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReniecMainScaffold(
    sesion: Sesion,
    darkTheme: Boolean,
    onCambiarTema: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val destinos = remember {
        listOf(
            DestinoNav(Screen.Inicio, "Inicio", Icons.Default.Home),
            DestinoNav(Screen.PadronDni, "Padrón DNI", Icons.Default.Badge),
            DestinoNav(Screen.Tramites, "Trámites", Icons.Default.Assignment),
            DestinoNav(Screen.Perfil, "Perfil", Icons.Default.Person)
        )
    }

    var pantallaActual by remember { mutableStateOf<Screen>(Screen.Inicio) }

    val ciudadanoViewModel: CiudadanoViewModel = koinViewModel()
    val tramiteViewModel: TramiteViewModel = koinViewModel()

    val ciudadanoState by ciudadanoViewModel.uiState.collectAsState()
    val tramiteState by tramiteViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titulo = destinos.firstOrNull { it.screen == pantallaActual }?.titulo ?: "RENIEC Móvil"
                    Text("RENIEC • $titulo")
                },
                actions = {
                    IconButton(onClick = { onCambiarTema(!darkTheme) }) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar Tema"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                destinos.forEach { destino ->
                    NavigationBarItem(
                        selected = pantallaActual == destino.screen,
                        onClick = { pantallaActual = destino.screen },
                        icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                        label = { Text(destino.titulo) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (pantallaActual) {
                Screen.Inicio -> InicioScreen(
                    sesion = sesion,
                    totalCiudadanos = ciudadanoState.ciudadanos.size,
                    tramites = tramiteState.tramites,
                    avisoRed = tramiteState.avisoRed ?: ciudadanoState.avisoRed,
                    sincronizando = tramiteState.sincronizando || ciudadanoState.sincronizando,
                    onSincronizarTodo = {
                        ciudadanoViewModel.sincronizar()
                        tramiteViewModel.sincronizar()
                    },
                    onNavegar = { pantallaActual = it },
                    onNuevoTramite = {
                        tramiteViewModel.abrirNuevoTramite()
                        pantallaActual = Screen.Tramites
                    }
                )
                Screen.PadronDni -> CiudadanoScreen(
                    viewModel = ciudadanoViewModel,
                    onIniciarTramiteParaCiudadano = { ciudadano ->
                        tramiteViewModel.abrirNuevoTramite(ciudadano)
                        pantallaActual = Screen.Tramites
                    }
                )
                Screen.Tramites -> TramiteScreen(
                    viewModel = tramiteViewModel
                )
                Screen.Perfil -> PerfilScreen(
                    sesion = sesion,
                    darkTheme = darkTheme,
                    onCambiarTema = onCambiarTema,
                    onCerrarSesion = onCerrarSesion
                )
            }
        }
    }
}
