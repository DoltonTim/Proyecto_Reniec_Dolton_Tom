package pe.edu.upeu.reniecmovil.presentation.ciudadano

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
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
    var ciudadanoParaAfis by remember { mutableStateOf<Ciudadano?>(null) }
    var ciudadanoParaEliminar by remember { mutableStateOf<Ciudadano?>(null) }

    val puedeEscribirPadron = sesion.puede("PADRON_ESCRIBIR")
    val puedeEliminarPadron = sesion.puede("PADRON_ELIMINAR") || sesion.puede("ROLE_ADMIN")
    val puedeCrearTramite = sesion.puede("TRAMITES_ESCRIBIR")

    ciudadanoParaAfis?.let { c ->
        DialogoVerificacionBiometricaAfis(
            ciudadano = c,
            onCerrar = { ciudadanoParaAfis = null }
        )
    }

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
                        text = "Padrón Nacional de Identificación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (puedeEliminarPadron) {
                            "Perfil Admin: Control total de padrón y depuración habilitada"
                        } else if (puedeEscribirPadron) {
                            "Perfil Brigadista: Empadronamiento activo (Borrado protegido)"
                        } else {
                            "Módulo Consulta: Solo búsqueda DNIe 3.0 y validación AFIS"
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

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.filtroDniONombre,
                    onValueChange = viewModel::onFiltroChange,
                    label = { Text("Buscar por DNI (8 dígitos), Apellidos o Ubigeo") },
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
                    onVerificarHuella = { ciudadanoParaAfis = seleccionado },
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { ciudadanoParaAfis = ciudadano }) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = "Verificar Huella AFIS",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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
    onVerificarHuella: () -> Unit,
    onEditar: () -> Unit
) {
    var mostrarReverso by remember(ciudadano.id) { mutableStateOf(false) }
    val digitoVerificador = remember(ciudadano.dni) {
        (ciudadano.dni.sumOf { it.digitToIntOrNull() ?: 0 } % 9) + 1
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
                            "DNIe 3.0 • DATOS BIOMÉTRICOS Y ZONA MRZ (REVERSO)"
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
                        text = "DNI ${ciudadano.dni}-$digitoVerificador",
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
                                    tint = Color(0xFFB45309),
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
                            text = "UBIGEO: ${ciudadano.ubigeo}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(66.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text("ÍNDICE DER.", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "DIRECCIÓN: ${ciudadano.direccion}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "DISTRITO / PROV.: ${ciudadano.ubigeo}",
                            style = MaterialTheme.typography.bodySmall
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
                            text = "I<PER${ciudadano.dni}<$digitoVerificador<<<<<<<<<<<<<<<",
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
                        text = if (!mostrarReverso) "Toca la tarjeta para girar el DNIe 3.0 (MRZ/QR)" else "Toca para volver al anverso",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

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
                        Text("Trámite", style = MaterialTheme.typography.labelMedium)
                    }
                }
                FilledTonalButton(
                    onClick = onVerificarHuella,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AFIS Huella", style = MaterialTheme.typography.labelMedium)
                }
                if (puedeEditar) {
                    OutlinedButton(
                        onClick = onEditar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoVerificacionBiometricaAfis(
    ciudadano: Ciudadano,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Verificación Biométrica AFIS - RENIEC",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "IDENTIDAD VERIFICADA (99.8% Match)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Text("Ciudadano: ${ciudadano.nombreCompleto}", fontWeight = FontWeight.Bold)
                Text("DNI / CUI: ${ciudadano.dni}", style = MaterialTheme.typography.bodySmall)
                Text("Dedo capturado: Índice Derecho (Minucias: 42/42)", style = MaterialTheme.typography.bodySmall)
                Text("Estado Padrón: HÁBIL SIN RESTRICCIONES", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = onCerrar) {
                Text("Aceptar Conformidad")
            }
        }
    )
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
