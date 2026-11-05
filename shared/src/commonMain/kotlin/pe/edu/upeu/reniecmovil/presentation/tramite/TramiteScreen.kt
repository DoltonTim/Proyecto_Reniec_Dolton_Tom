package pe.edu.upeu.reniecmovil.presentation.tramite

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.reniecmovil.domain.model.EstadoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.TipoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni

@Composable
fun TramiteScreen(
    viewModel: TramiteViewModel
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gestión de Trámites de DNI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "CRUD REST + Sincronización Offline-First SQLDelight",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = viewModel::sincronizar,
                        enabled = !state.sincronizando
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Sync")
                    }
                    Button(onClick = { viewModel.abrirNuevoTramite() }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Nuevo")
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = state.filtroBusqueda,
                onValueChange = viewModel::onFiltroBusquedaChange,
                label = { Text("Filtrar por DNI, Ciudadano, Tipo o Ubigeo") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val opciones = listOf("TODOS") + EstadoTramiteDni.entries.map { it.name }
                opciones.forEach { opcion ->
                    FilterChip(
                        selected = state.filtroEstado == opcion,
                        onClick = { viewModel.onFiltroEstadoChange(opcion) },
                        label = { Text(opcion) }
                    )
                }
            }
        }

        if (state.mensajeExito != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.mensajeExito.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (state.mensajeError != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.mensajeError.orEmpty(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (state.mostrarFormulario) {
            item {
                FormularioTramiteCard(
                    esEdicion = state.editandoId != null,
                    form = state.formulario,
                    guardando = state.guardando,
                    onCambio = viewModel::actualizarFormulario,
                    onGuardar = viewModel::guardar,
                    onCancelar = viewModel::cerrarFormulario
                )
            }
        }

        items(state.tramitesFiltrados, key = { it.id }) { tramite ->
            TarjetaTramiteItem(
                tramite = tramite,
                onEditar = { viewModel.iniciarEdicion(tramite) },
                onMarcarAtendido = { viewModel.cambiarEstadoRapido(tramite, EstadoTramiteDni.ATENDIDO) },
                onEliminar = { viewModel.eliminar(tramite.id) }
            )
        }
    }
}

@Composable
private fun TarjetaTramiteItem(
    tramite: TramiteDni,
    onEditar: () -> Unit,
    onMarcarAtendido: () -> Unit,
    onEliminar: () -> Unit
) {
    val tipo = TipoTramiteDni.desdeTexto(tramite.tipoTramite)
    val estado = EstadoTramiteDni.desdeTexto(tramite.estado)
    val esOffline = tramite.pendienteSync || tramite.id < 0

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
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
                Text(
                    text = "DNI ${tramite.dniCiudadano} • ${tramite.nombreCiudadano}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = if (esOffline) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (esOffline) Icons.Default.Schedule else Icons.Default.CloudDone,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (esOffline) "Pendiente Sync" else "Sincronizado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "Tipo: ${tipo.etiqueta} (Tasa S/ ${tipo.costoSoles}0) | Estado: ${estado.etiqueta}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Ubigeo / Agencia: ${tramite.ubigeoDestino} | Fecha: ${tramite.fechaRegistro}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Observación: ${tramite.observaciones}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (estado != EstadoTramiteDni.ATENDIDO) {
                    IconButton(onClick = onMarcarAtendido) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Marcar Atendido",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Trámite")
                }
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Anular Trámite",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun FormularioTramiteCard(
    esEdicion: Boolean,
    form: FormularioTramite,
    guardando: Boolean,
    onCambio: ((FormularioTramite) -> FormularioTramite) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (esEdicion) "Actualizar Trámite de DNI" else "Registrar Solicitud de Trámite DNI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = form.dniCiudadano,
                onValueChange = { v -> onCambio { it.copy(dniCiudadano = v.take(8), dniError = null) } },
                label = { Text("DNI del Ciudadano (8 dígitos)") },
                isError = form.dniError != null,
                supportingText = form.dniError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.nombreCiudadano,
                onValueChange = { v -> onCambio { it.copy(nombreCiudadano = v, nombreError = null) } },
                label = { Text("Apellidos y Nombres del Ciudadano") },
                isError = form.nombreError != null,
                supportingText = form.nombreError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipo de Trámite RENIEC:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TipoTramiteDni.entries.forEach { tipo ->
                    FilterChip(
                        selected = form.tipoTramite == tipo.name,
                        onClick = { onCambio { it.copy(tipoTramite = tipo.name) } },
                        label = { Text(tipo.etiqueta) }
                    )
                }
            }

            Text("Estado del Trámite:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EstadoTramiteDni.entries.forEach { est ->
                    FilterChip(
                        selected = form.estado == est.name,
                        onClick = { onCambio { it.copy(estado = est.name) } },
                        label = { Text(est.etiqueta) }
                    )
                }
            }

            OutlinedTextField(
                value = form.ubigeoDestino,
                onValueChange = { v -> onCambio { it.copy(ubigeoDestino = v, ubigeoError = null) } },
                label = { Text("Agencia / Ubigeo de Atención") },
                isError = form.ubigeoError != null,
                supportingText = form.ubigeoError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.observaciones,
                onValueChange = { v -> onCambio { it.copy(observaciones = v, observacionesError = null) } },
                label = { Text("Sustento / Observaciones del Trámite") },
                isError = form.observacionesError != null,
                supportingText = form.observacionesError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onGuardar,
                    enabled = !guardando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (esEdicion) "Actualizar Trámite" else "Registrar Trámite")
                }
                OutlinedButton(
                    onClick = onCancelar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
