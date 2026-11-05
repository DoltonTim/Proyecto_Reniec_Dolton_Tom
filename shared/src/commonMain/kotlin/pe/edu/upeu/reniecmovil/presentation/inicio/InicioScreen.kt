package pe.edu.upeu.reniecmovil.presentation.inicio

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.reniecmovil.domain.model.EstadoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.domain.model.TipoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni
import pe.edu.upeu.reniecmovil.navigation.Screen

@Composable
fun InicioScreen(
    sesion: Sesion,
    totalCiudadanos: Int,
    tramites: List<TramiteDni>,
    avisoRed: String?,
    sincronizando: Boolean,
    onSincronizarTodo: () -> Unit,
    onNavegar: (Screen) -> Unit,
    onNuevoTramite: () -> Unit
) {
    val pendientesSync = tramites.count { it.pendienteSync || it.id < 0 }
    val sincronizados = tramites.size - pendientesSync
    val atendidos = tramites.count { it.estado == EstadoTramiteDni.ATENDIDO.name }
    val tasaRecaudada = tramites.sumOf { TipoTramiteDni.desdeTexto(it.tipoTramite).costoSoles }

    val progresoAtencion by animateFloatAsState(
        targetValue = if (tramites.isEmpty()) 0f else atendidos.toFloat() / tramites.size.toFloat()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner de estado de red Online / Offline-First
        Surface(
            color = if (avisoRed == null) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (avisoRed == null) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = if (avisoRed == null) {
                                "CONECTADO A CENTRAL RENIEC (ONLINE)"
                            } else {
                                "MODO BRIGADA OFFLINE (SQLDelight)"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = avisoRed ?: "Sincronización bidireccional REST + JWT activa",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                FilledTonalButton(
                    onClick = onSincronizarTodo,
                    enabled = !sincronizando
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(if (sincronizando) "..." else "Sync")
                }
            }
        }

        // Tarjeta de bienvenida del Registrador
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bienvenido, ${sesion.nombreCompleto}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${sesion.cargo} • ${sesion.sede}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Recaudación Tasa Banco de la Nación: S/ ${tasaRecaudada}0",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Verified, contentDescription = null)
                    }
                }
            }
        }

        // Cuadrícula de métricas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaMetrica(
                titulo = "Padrón DNI",
                valor = totalCiudadanos.toString(),
                subtitulo = "Ciudadanos empadronados",
                modifier = Modifier.weight(1f)
            )
            TarjetaMetrica(
                titulo = "Trámites DNI",
                valor = tramites.size.toString(),
                subtitulo = "Solicitudes registradas",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaMetrica(
                titulo = "Sincronizados",
                valor = sincronizados.toString(),
                subtitulo = "En Servidor Central",
                modifier = Modifier.weight(1f)
            )
            TarjetaMetrica(
                titulo = "Offline Pend.",
                valor = pendientesSync.toString(),
                subtitulo = "En SQLDelight local",
                modifier = Modifier.weight(1f)
            )
        }

        // Panel gráfico de avance de campaña y distribución por tipo de trámite
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Indicadores de Brigada RENIEC",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                val porcentaje = (progresoAtencion * 100).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Meta de entregas DNIe completadas",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "$atendidos de ${tramites.size} ($porcentaje%)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { progresoAtencion },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Distribución por Tipo de Trámite:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                TipoTramiteDni.entries.forEach { tipo ->
                    val cantidad = tramites.count { it.tipoTramite == tipo.name }
                    val fraccion = if (tramites.isEmpty()) 0f else cantidad.toFloat() / tramites.size.toFloat()
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = tipo.etiqueta,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "$cantidad solic. (S/ ${tipo.costoSoles.toInt()})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                                    .height(6.dp)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                        }
                    }
                }
            }
        }

        // Botones de acción rápida
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onNavegar(Screen.PadronDni) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Padrón DNIe")
            }
            FilledTonalButton(
                onClick = onNuevoTramite,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Nuevo Trámite")
            }
        }

        Text(
            text = "Actividad Reciente de Identificación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        tramites.take(4).forEach { tramite ->
            val tipo = TipoTramiteDni.desdeTexto(tramite.tipoTramite)
            val estado = EstadoTramiteDni.desdeTexto(tramite.estado)
            ElevatedCard(
                onClick = { onNavegar(Screen.Tramites) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DNI ${tramite.dniCiudadano} • ${tramite.nombreCiudadano}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tipo.etiqueta} (${tramite.ubigeoDestino})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = if (tramite.pendienteSync || tramite.id < 0) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (tramite.pendienteSync || tramite.id < 0) {
                                "OFFLINE"
                            } else {
                                estado.etiqueta
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaMetrica(
    titulo: String,
    valor: String,
    subtitulo: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
