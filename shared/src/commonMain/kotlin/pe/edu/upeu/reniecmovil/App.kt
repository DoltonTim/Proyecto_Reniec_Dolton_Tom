package pe.edu.upeu.reniecmovil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
fun App() {
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
            DestinoNav(Screen.Tramites, "Trámites", Icons.AutoMirrored.Filled.Assignment),
            DestinoNav(Screen.Perfil, "Perfil", Icons.Default.Person)
        )
    }

    var pantallaActual by remember { mutableStateOf<Screen>(Screen.Inicio) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val ciudadanoViewModel: CiudadanoViewModel = koinViewModel()
    val tramiteViewModel: TramiteViewModel = koinViewModel()

    val ciudadanoState by ciudadanoViewModel.uiState.collectAsState()
    val tramiteState by tramiteViewModel.uiState.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "RENIEC MÓVIL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = sesion.nombreCompleto,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "@${sesion.usuario} • ${sesion.sede}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                destinos.forEach { destino ->
                    NavigationDrawerItem(
                        label = { Text(destino.titulo) },
                        selected = pantallaActual == destino.screen,
                        onClick = {
                            pantallaActual = destino.screen
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCerrarSesion()
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Modo Oscuro Material 3", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = darkTheme, onCheckedChange = onCambiarTema)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val titulo = destinos.firstOrNull { it.screen == pantallaActual }?.titulo ?: "RENIEC Móvil"
                        Text("RENIEC • $titulo")
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú lateral")
                        }
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
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
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
}
