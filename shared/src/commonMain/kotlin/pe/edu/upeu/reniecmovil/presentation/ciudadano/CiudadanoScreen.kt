package pe.edu.upeu.reniecmovil.presentation.ciudadano

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.EstadoCivil

@Composable
fun CiudadanoScreen(
    viewModel: CiudadanoViewModel,
    onIniciarTramiteParaCiudadano: (Ciudadano) -> Unit
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
                        text = "Padrón Nacional de Identificación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Consulta por DNI (8 dígitos), registro y actualización",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = viewModel::abrirNuevoCiudadano) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Empadronar")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.filtroDniONombre,
                    onValueChange = viewModel::onFiltroChange,
                    label = { Text("Buscar por DNI, Apellidos o Ubigeo") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(
                    onClick = viewModel::consultarDniExacto,
                    modifier = Modifier.height(54.dp)
                ) {
                    Text("Validar DNI")
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
                FormularioCiudadanoCard(
                    esEdicion = state.editandoId != null,
                    form = state.formulario,
                    guardando = state.guardando,
                    onCambio = viewModel::actualizarFormulario,
                    onGuardar = viewModel::guardarCiudadano,
                    onCancelar = viewModel::cerrarFormulario
                )
            }
        }

        state.ciudadanoSeleccionado?.let { seleccionado ->
            item {
                TarjetaDniPeruano(
                    ciudadano = seleccionado,
                    onIniciarTramite = { onIniciarTramiteParaCiudadano(seleccionado) },
                    onEditar = { viewModel.iniciarEdicion(seleccionado) }
                )
            }
        }

        item {
            Text(
                text = "Ciudadanos en Padrón (${state.ciudadanosFiltrados.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(state.ciudadanosFiltrados, key = { it.id }) { ciudadano ->
            ElevatedCard(
                onClick = { viewModel.seleccionarCiudadano(ciudadano) },
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
                            text = "DNI: ${ciudadano.dni} • ${ciudadano.nombreCompleto}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${ciudadano.ubigeo} | Nac: ${ciudadano.fechaNacimiento}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row {
                        IconButton(onClick = { viewModel.iniciarEdicion(ciudadano) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { viewModel.eliminar(ciudadano.id) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaDniPeruano(
    ciudadano: Ciudadano,
    onIniciarTramite: () -> Unit,
    onEditar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REPÚBLICA DEL PERÚ • RENIEC",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "DOCUMENTO NACIONAL DE IDENTIDAD",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "CUI / DNI: ${ciudadano.dni}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(74.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "APELLIDOS: ${ciudadano.apellidoPaterno} ${ciudadano.apellidoMaterno}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "NOMBRES: ${ciudadano.nombres}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "FECHA NAC.: ${ciudadano.fechaNacimiento} | EST. CIVIL: ${ciudadano.estadoCivil}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "UBIGEO: ${ciudadano.ubigeo}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "DOMICILIO: ${ciudadano.direccion}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "DONACIÓN DE ÓRGANOS: ${if (ciudadano.donacionOrganos) "SÍ AUTORIZA" else "NO ESPECIFICA"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onIniciarTramite,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Iniciar Trámite DNI")
                }
                OutlinedButton(
                    onClick = onEditar,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Actualizar Datos")
                }
            }
        }
    }
}

@Composable
private fun FormularioCiudadanoCard(
    esEdicion: Boolean,
    form: FormularioCiudadano,
    guardando: Boolean,
    onCambio: ((FormularioCiudadano) -> FormularioCiudadano) -> Unit,
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
                text = if (esEdicion) "Editar Ficha Ciudadana RENIEC" else "Empadronar Nuevo Ciudadano",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = form.dni,
                onValueChange = { valor -> onCambio { it.copy(dni = valor.take(8), dniError = null) } },
                label = { Text("Número de DNI (8 dígitos)") },
                isError = form.dniError != null,
                supportingText = form.dniError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.nombres,
                onValueChange = { valor -> onCambio { it.copy(nombres = valor, nombresError = null) } },
                label = { Text("Nombres Completos") },
                isError = form.nombresError != null,
                supportingText = form.nombresError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = form.apellidoPaterno,
                    onValueChange = { valor -> onCambio { it.copy(apellidoPaterno = valor, paternoError = null) } },
                    label = { Text("Apellido Paterno") },
                    isError = form.paternoError != null,
                    supportingText = form.paternoError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = form.apellidoMaterno,
                    onValueChange = { valor -> onCambio { it.copy(apellidoMaterno = valor, maternoError = null) } },
                    label = { Text("Apellido Materno") },
                    isError = form.maternoError != null,
                    supportingText = form.maternoError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = form.fechaNacimiento,
                onValueChange = { valor -> onCambio { it.copy(fechaNacimiento = valor, fechaError = null) } },
                label = { Text("Fecha de Nacimiento (AAAA-MM-DD)") },
                isError = form.fechaError != null,
                supportingText = form.fechaError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Estado Civil:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EstadoCivil.entries.forEach { ec ->
                    FilterChip(
                        selected = form.estadoCivil == ec.name,
                        onClick = { onCambio { it.copy(estadoCivil = ec.name) } },
                        label = { Text(ec.etiqueta, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OutlinedTextField(
                value = form.ubigeo,
                onValueChange = { valor -> onCambio { it.copy(ubigeo = valor, ubigeoError = null) } },
                label = { Text("Ubigeo (Departamento / Provincia / Distrito)") },
                isError = form.ubigeoError != null,
                supportingText = form.ubigeoError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.direccion,
                onValueChange = { valor -> onCambio { it.copy(direccion = valor, direccionError = null) } },
                label = { Text("Dirección Domiciliaria Actual") },
                isError = form.direccionError != null,
                supportingText = form.direccionError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = form.donacionOrganos,
                    onCheckedChange = { activo -> onCambio { it.copy(donacionOrganos = activo) } }
                )
                Text("Autoriza donación de órganos en el DNI", style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onGuardar,
                    enabled = !guardando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (esEdicion) "Guardar Cambios" else "Registrar en Padrón")
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
