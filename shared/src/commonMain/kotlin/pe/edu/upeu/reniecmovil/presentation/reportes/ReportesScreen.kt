package pe.edu.upeu.reniecmovil.presentation.reportes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var tabSeleccionada by remember { mutableStateOf(0) } // 0: Gráficos Estadísticos, 1: Historial Detallado

    fun cargarReportes() {
        scope.launch {
            cargando = true
            runCatching {
                auditoriaApi.listarRecientes(limite = 150)
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

    // Cálculos estadísticos agregados
    val totalEventos = eventosFiltrados.size
    val porOperador = remember(eventosFiltrados) {
        eventosFiltrados.groupBy { it.usuario }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    val porEntidad = remember(eventosFiltrados) {
        eventosFiltrados.groupBy { it.entidad.ifBlank { "GENERAL" } }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    val porAccion = remember(eventosFiltrados) {
        eventosFiltrados.groupBy { it.accion }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
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
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Panel Estadístico y Reportes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Métricas de productividad y auditoría legal en tiempo real",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { cargarReportes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }

                // KPIs Rápidos en Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardKpi(
                        titulo = "Operaciones",
                        valor = "$totalEventos",
                        subtitulo = "Acciones en BD",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    CardKpi(
                        titulo = "Operadores",
                        valor = "${operadoresUnicos.size - 1}",
                        subtitulo = "Activos/Cesados",
                        color = Color(0xFFC6FF00),
                        modifier = Modifier.weight(1f)
                    )
                    CardKpi(
                        titulo = "Categorías",
                        valor = "${porEntidad.size}",
                        subtitulo = "Módulos RENIEC",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Selector de Pestaña: Estadísticas vs Historial
        TabRow(
            selectedTabIndex = tabSeleccionada,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = tabSeleccionada == 0,
                onClick = { tabSeleccionada = 0 },
                text = { Text("📊 Gráficos Estadísticos", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = tabSeleccionada == 1,
                onClick = { tabSeleccionada = 1 },
                text = { Text("📋 Historial Inmutable", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        // Filtro por Operador
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Filtrar por Operador:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                operadoresUnicos.take(5).forEach { op ->
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

        // Contenido según la pestaña seleccionada
        if (tabSeleccionada == 0) {
            // VISTA 1: ESTADÍSTICAS Y GRÁFICOS DE BARRAS
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Gráfico 1: Productividad por Operador
                item {
                    CardGraficoEstadistico(
                        titulo = "📈 Productividad por Operador",
                        descripcion = "Total de trámites, empadronamientos y biometría registrados por usuario",
                        datos = porOperador.map { Pair("@${it.first}", it.second) },
                        total = totalEventos,
                        colorBarra = Color(0xFFC6FF00)
                    )
                }

                // Gráfico 2: Distribución por Módulo Institucional
                item {
                    CardGraficoEstadistico(
                        titulo = "🏛️ Actividad por Módulo Institucional",
                        descripcion = "Proporción de eventos entre Padrón, Trámites, Biometría y Auditoría",
                        datos = porEntidad.map { Pair(it.first, it.second) },
                        total = totalEventos,
                        colorBarra = MaterialTheme.colorScheme.primary
                    )
                }

                // Gráfico 3: Tipo de Acción Ejecutada
                item {
                    CardGraficoEstadistico(
                        titulo = "⚡ Tipos de Acción Frecuentes",
                        descripcion = "Operaciones más recurrentes ejecutadas en el sistema",
                        datos = porAccion.take(6).map { Pair(it.first, it.second) },
                        total = totalEventos,
                        colorBarra = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        } else {
            // VISTA 2: LISTA DE HISTORIAL AUDITADO
            if (eventosFiltrados.isEmpty() && !cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron registros de auditoría.",
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
                        CardItemHistorial(ev)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardKpi(
    titulo: String,
    valor: String,
    subtitulo: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun CardGraficoEstadistico(
    titulo: String,
    descripcion: String,
    datos: List<Pair<String, Int>>,
    total: Int,
    colorBarra: Color
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            if (datos.isEmpty() || total == 0) {
                Text(
                    text = "Sin datos estadísticos registrados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                datos.forEach { (etiqueta, valor) ->
                    val porcentaje = (valor.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    val porcentajeTexto = (porcentaje * 100).toInt()

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = etiqueta,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$valor ($porcentajeTexto%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorBarra
                            )
                        }

                        // Barra de progreso estilizada
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = porcentaje)
                                    .fillMaxHeight()
                                    .background(colorBarra)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardItemHistorial(ev: AuditoriaDto) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
