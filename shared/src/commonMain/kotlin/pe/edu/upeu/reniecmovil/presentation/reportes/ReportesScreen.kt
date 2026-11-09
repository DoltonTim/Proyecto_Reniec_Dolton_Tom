package pe.edu.upeu.reniecmovil.presentation.reportes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import pe.edu.upeu.reniecmovil.data.remote.AuditoriaApi
import pe.edu.upeu.reniecmovil.data.remote.dto.AuditoriaDto
import pe.edu.upeu.reniecmovil.domain.model.Sesion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    sesion: Sesion,
    onVolver: () -> Unit = {}
) {
    val auditoriaApi: AuditoriaApi = koinInject()
    val scope = rememberCoroutineScope()

    var eventos by remember { mutableStateOf<List<AuditoriaDto>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var filtroOperador by remember { mutableStateOf("") }
    var filtroAccion by remember { mutableStateOf("TODAS") }

    fun cargarReportes() {
        scope.launch {
            cargando = true
            runCatching {
                auditoriaApi.listarRecientes(limite = 100)
            }.onSuccess { lista ->
                eventos = lista
            }.onFailure {
                // Si falla o está offline, mantiene los que tenga
            }
            cargando = false
        }
    }

    LaunchedEffect(Unit) {
        cargarReportes()
    }

    val operadoresUnicos = remember(eventos) {
        listOf("TODOS") + eventos.map { it.usuario }.distinct().sorted()
    }

    val eventosFiltrados = remember(eventos, filtroOperador, filtroAccion) {
        eventos.filter { ev ->
            val matchOp = filtroOperador.isBlank() || filtroOperador == "TODOS" || ev.usuario.equals(filtroOperador, ignoreCase = true)
            val matchAcc = filtroAccion == "TODAS" || ev.accion.contains(filtroAccion, ignoreCase = true)
            matchOp && matchAcc
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cabecera Informativa
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Reportes y Auditoría de Operadores",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Historial legal inmutable de empadronamientos y trámites",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { cargarReportes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚖️ Base Legal: Las acciones realizadas por operadores cesados o dados de baja permanecen auditadas por seguridad institucional de RENIEC.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Filtro por Operador
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Filtrar por Operador / Registrador:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                operadoresUnicos.take(4).forEach { op ->
                    FilterChip(
                        selected = (filtroOperador.isBlank() && op == "TODOS") || filtroOperador.equals(op, ignoreCase = true),
                        onClick = { filtroOperador = if (op == "TODOS") "" else op },
                        label = {
                            Text(
                                text = if (op == "TODOS") "Todos" else "@$op",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }
        }

        // Resumen Contador
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Actividades registradas (${eventosFiltrados.size}):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold
            )
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }

        // Lista de Registros de Auditoría
        if (eventosFiltrados.isEmpty() && !cargando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron registros de auditoría con los filtros aplicados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(eventosFiltrados, key = { it.id }) { ev ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "@${ev.usuario}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = ev.rol,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                ev.createdAt?.let { fecha ->
                                    Text(
                                        text = fecha.take(19).replace("T", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "Acción: ${ev.accion} [${ev.entidad}]",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = ev.detalle,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "DNI Ref: ${ev.referenciaDni}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Dispositivo: ${ev.dispositivo}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
