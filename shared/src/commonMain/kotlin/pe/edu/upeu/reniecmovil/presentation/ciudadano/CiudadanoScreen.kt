package pe.edu.upeu.reniecmovil.presentation.ciudadano

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.reniecmovil.domain.model.CatalogoUbigeoReniec
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.EstadoCivil
import pe.edu.upeu.reniecmovil.domain.model.Sesion

@Composable
fun CiudadanoScreen(
    viewModel: CiudadanoViewModel,
    sesion: Sesion,
    onIniciarTramiteParaCiudadano: (Ciudadano) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var ciudadanoParaEliminar by remember { mutableStateOf<Ciudadano?>(null) }

    val puedeEscribirPadron = sesion.puede("PADRON_ESCRIBIR")
    val puedeEliminarPadron = sesion.puede("PADRON_ELIMINAR") || sesion.puede("ROLE_ADMIN")
    val puedeCrearTramite = sesion.puede("TRAMITES_ESCRIBIR")

    ciudadanoParaEliminar?.let { c ->
        AlertDialog(
            onDismissRequest = { ciudadanoParaEliminar = null },
            title = { Text("Confirmar Eliminación en Padrón") },
            text = {
                Text("¿Está seguro de eliminar el registro del ciudadano ${c.nombreCompleto} con DNI ${c.dni}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminar(c.id)
                        ciudadanoParaEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { ciudadanoParaEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

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
                        text = "Padrón Nacional PIDE • API RENIEC",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (puedeEliminarPadron) {
                            "Perfil Admin: Control total por Departamento, Provincia y Distrito"
                        } else if (puedeEscribirPadron) {
                            "Perfil Brigadista: Empadronamiento y actualización de ciudadanos"
                        } else {
                            "Módulo Consulta: Búsqueda por DNIe 3.0 y Ubigeo INEI/RENIEC"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (puedeEscribirPadron) {
                    Button(onClick = viewModel::abrirNuevoCiudadano) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Empadronar")
                    }
                }
            }
        }

        // Filtro por Departamento (Ubigeo RENIEC)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Filtrar Padrón por Departamento (Ubigeo INEI/RENIEC):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CatalogoUbigeoReniec.departamentos.forEach { dep ->
                        FilterChip(
                            selected = state.filtroDepartamento == dep,
                            onClick = { viewModel.onFiltroDepartamentoChange(dep) },
                            label = {
                                Text(
                                    text = if (dep == "TODOS") "TODOS (PERÚ)" else dep,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }
                }
            }
        }

        // Buscador por DNI, Nombre o Ubigeo
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.filtroDniONombre,
                    onValueChange = viewModel::onFiltroChange,
                    label = { Text("Buscar por DNI (8 dígitos), Apellidos o Distrito") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(
                    onClick = viewModel::consultarDniExacto,
                    modifier = Modifier.height(54.dp)
                ) {
                    Text("API DNI")
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

        if (state.mostrarFormulario && puedeEscribirPadron) {
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
                TarjetaDniElectronicoInteractivo(
                    ciudadano = seleccionado,
                    puedeCrearTramite = puedeCrearTramite,
                    puedeEditar = puedeEscribirPadron,
                    onIniciarTramite = { onIniciarTramiteParaCiudadano(seleccionado) },
                    onEditar = { viewModel.iniciarEdicion(seleccionado) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ciudadanos en ${if (state.filtroDepartamento == "TODOS") "Todo el Perú" else state.filtroDepartamento} (${state.ciudadanosFiltrados.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (state.filtroDniONombre.isNotBlank()) {
                    TextButton(onClick = { viewModel.onFiltroChange("") }) {
                        Text("Ver todos", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(state.ciudadanosFiltrados, key = { it.id }) { ciudadano ->
            val infoUbigeo = remember(ciudadano.ubigeo) {
                CatalogoUbigeoReniec.buscarUbigeo(ciudadano.ubigeo)
            }
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
                            text = if (infoUbigeo != null) {
                                "Ubigeo ${infoUbigeo.codigo}: ${ciudadano.ubigeo}"
                            } else {
                                ciudadano.ubigeo
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Domicilio: ${ciudadano.direccion} | Nac: ${ciudadano.fechaNacimiento}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (puedeEscribirPadron) {
                            IconButton(onClick = { viewModel.iniciarEdicion(ciudadano) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                        }
                        if (puedeEliminarPadron) {
                            IconButton(onClick = { ciudadanoParaEliminar = ciudadano }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        } else if (puedeEscribirPadron) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Borrado protegido en Brigada",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaDniElectronicoInteractivo(
    ciudadano: Ciudadano,
    puedeCrearTramite: Boolean,
    puedeEditar: Boolean,
    onIniciarTramite: () -> Unit,
    onEditar: () -> Unit
) {
    var mostrarReverso by remember(ciudadano.id) { mutableStateOf(false) }
    val digitoVerificador = remember(ciudadano.dni) {
        (ciudadano.dni.sumOf { it.digitToIntOrNull() ?: 0 } % 9) + 1
    }
    val ubigeoCatalogo = remember(ciudadano.ubigeo) {
        CatalogoUbigeoReniec.buscarUbigeo(ciudadano.ubigeo)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { mostrarReverso = !mostrarReverso },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                        text = if (!mostrarReverso) {
                            "DOCUMENTO NACIONAL DE IDENTIDAD ELECTRÓNICO (ANVERSO)"
                        } else {
                            "DNIe 3.0 • DATOS DE UBIGEO Y ZONA MRZ (REVERSO)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "CUI ${ciudadano.dni}-$digitoVerificador",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            if (!mostrarReverso) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD97706).copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Memory,
                                    contentDescription = "Chip Criptográfico",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "CHIP NFC",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "PRIMER APELLIDO: ${ciudadano.apellidoPaterno}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "SEGUNDO APELLIDO: ${ciudadano.apellidoMaterno}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "PRENOMBRES: ${ciudadano.nombres}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "NACIMIENTO: ${ciudadano.fechaNacimiento} | EST. CIVIL: ${ciudadano.estadoCivil}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "UBIGEO (${ubigeoCatalogo?.codigo ?: "150101"}): ${ciudadano.ubigeo}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "DOMICILIO: ${ciudadano.direccion}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "DPTO / PROV / DIST: ${ciudadano.ubigeo}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "SEDE RENIEC: ${ubigeoCatalogo?.agenciaReniec ?: "Oficina Registral RENIEC"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "DONACIÓN DE ÓRGANOS: ${if (ciudadano.donacionOrganos) "SÍ AUTORIZA" else "NO ESPECIFICA"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(66.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val apPat = ciudadano.apellidoPaterno.replace(" ", "<").padEnd(12, '<').take(12)
                    val nom = ciudadano.nombres.replace(" ", "<").padEnd(14, '<').take(14)
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "I<PER${ciudadano.dni}<$digitoVerificador<<<${ubigeoCatalogo?.codigo ?: "150101"}<<<<<<<",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "9905144M3011045PER<<<<<<<<<<<6",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$apPat<<$nom",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.FlipCameraAndroid,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (!mostrarReverso) "Toca la tarjeta para girar el DNIe 3.0 (Ubigeo/MRZ/QR)" else "Toca para volver al anverso",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (puedeCrearTramite || puedeEditar) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (puedeCrearTramite) {
                        Button(
                            onClick = onIniciarTramite,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Iniciar Trámite", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (puedeEditar) {
                        OutlinedButton(
                            onClick = onEditar,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar Ficha", style = MaterialTheme.typography.labelMedium)
                        }
                    }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
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

            Text(
                text = "Seleccionar Ubigeo Rápido (Dpto / Prov / Distrito):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CatalogoUbigeoReniec.sedes.forEach { sede ->
                    FilterChip(
                        selected = form.ubigeo.contains(sede.distrito, ignoreCase = true) || form.ubigeo.contains(sede.codigo),
                        onClick = {
                            onCambio {
                                it.copy(
                                    ubigeo = sede.etiquetaCompleta,
                                    ubigeoError = null
                                )
                            }
                        },
                        label = { Text(sede.etiquetaCorta, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OutlinedTextField(
                value = form.ubigeo,
                onValueChange = { valor -> onCambio { it.copy(ubigeo = valor, ubigeoError = null) } },
                label = { Text("Ubigeo Oficial (Código - Departamento / Provincia / Distrito)") },
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
                    Text(if (esEdicion) "Guardar Cambios" else "Registrar Ciudadano")
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
