package pe.edu.upeu.reniecmovil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import pe.edu.upeu.reniecmovil.theme.StoriDarkCard
import pe.edu.upeu.reniecmovil.theme.StoriLavenderMuted
import pe.edu.upeu.reniecmovil.theme.StoriNeonLime
import pe.edu.upeu.reniecmovil.theme.StoriNeonTextOnLime
import pe.edu.upeu.reniecmovil.theme.StoriPurpleMid
import pe.edu.upeu.reniecmovil.theme.StoriPurpleTop

private data class DestinoNav(
    val screen: Screen,
    val titulo: String,
    val icono: ImageVector
)

@Composable
fun App() {
    val sesionViewModel: SesionViewModel = koinViewModel()
    val sesionState by sesionViewModel.estado.collectAsState()
    var darkTheme by rememberSaveable { mutableStateOf(true) }

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
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                // Cabecera del Drawer con degradado púrpura/guinda y rombo tipo The Stori
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(StoriPurpleTop, StoriPurpleMid)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .rotate(45f)
                                .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                                .padding(5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(1.5.dp, StoriNeonLime, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(-45f)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Reniec Móvil",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = sesion.nombreCompleto,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = StoriNeonLime
                            )
                            Text(
                                text = "@${sesion.usuario} • ${sesion.cargo}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                destinos.forEach { destino ->
                    NavigationDrawerItem(
                        label = { Text(destino.titulo, fontWeight = FontWeight.Bold) },
                        selected = pantallaActual == destino.screen,
                        onClick = {
                            pantallaActual = destino.screen
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = StoriNeonLime,
                            selectedIconColor = StoriNeonLime
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                NavigationDrawerItem(
                    label = { Text("Cerrar sesión (${sesion.usuario})", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCerrarSesion()
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (darkTheme) "Skin Cyber-Purple (Oscuro)" else "Skin Institucional (Claro)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(checked = darkTheme, onCheckedChange = onCambiarTema)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(StoriPurpleTop, StoriPurpleMid)
                            )
                        )
                ) {
                    TopAppBar(
                        title = {
                            val titulo = destinos.firstOrNull { it.screen == pantallaActual }?.titulo ?: "RENIEC Móvil"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .rotate(45f)
                                        .border(1.5.dp, StoriNeonLime, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(13.dp)
                                            .rotate(-45f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "$titulo • ${sesion.usuario.uppercase()}",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
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
                                    contentDescription = "Cambiar Tema",
                                    tint = StoriNeonLime
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = StoriNeonLime
                        )
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = if (darkTheme) StoriDarkCard else MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    destinos.forEach { destino ->
                        NavigationBarItem(
                            selected = pantallaActual == destino.screen,
                            onClick = { pantallaActual = destino.screen },
                            icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                            label = { Text(destino.titulo, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (darkTheme) StoriNeonTextOnLime else Color.White,
                                selectedTextColor = if (darkTheme) StoriNeonLime else StoriPurpleMid,
                                indicatorColor = if (darkTheme) StoriNeonLime else StoriPurpleMid,
                                unselectedIconColor = if (darkTheme) StoriLavenderMuted else MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = if (darkTheme) StoriLavenderMuted else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
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
                        sesion = sesion,
                        onIniciarTramiteParaCiudadano = { ciudadano ->
                            tramiteViewModel.abrirNuevoTramite(ciudadano)
                            pantallaActual = Screen.Tramites
                        }
                    )
                    Screen.Tramites -> TramiteScreen(
                        viewModel = tramiteViewModel,
                        sesion = sesion
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
