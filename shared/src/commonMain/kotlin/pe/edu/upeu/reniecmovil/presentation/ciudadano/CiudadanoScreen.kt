package pe.edu.upeu.reniecmovil.presentation.ciudadano

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.reniecmovil.BiometriaSesionCache
import pe.edu.upeu.reniecmovil.domain.model.CatalogoUbigeoReniec
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.EstadoCivil
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.presentation.componentes.DialogoVerificacionBiometrica
import pe.edu.upeu.reniecmovil.presentation.componentes.LienzoHuellaSimulada
import pe.edu.upeu.reniecmovil.presentation.componentes.LienzoQrSimulado
import pe.edu.upeu.reniecmovil.presentation.componentes.SelectorUbigeoDesplegableReniec
import pe.edu.upeu.reniecmovil.presentation.componentes.TarjetaCapturaBiometricaFormulario
import pe.edu.upeu.reniecmovil.presentation.componentes.formatearMiles

@Composable
fun CiudadanoScreen(
    viewModel: CiudadanoViewModel,
    sesion: Sesion,
    onIniciarTramiteParaCiudadano: (Ciudadano) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var ciudadanoParaEliminar by remember { mutableStateOf<Ciudadano?>(null) }
    var versionBiometria by remember { mutableStateOf(0) }

    val puedeEscribirPadron = sesion.puede("PADRON_ESCRIBIR")
    val puedeEliminarPadron = sesion.puede("PADRON_ELIMINAR") || sesion.puede("ROLE_ADMIN")
    val puedeCrearTramite = sesion.puede("TRAMITES_ESCRIBIR")
    val rolUsuario = remember(sesion) {
        when {
            sesion.puede("ROLE_ADMIN") -> "ROLE_ADMIN"
            sesion.puede("ROLE_BRIGADISTA") -> "ROLE_BRIGADISTA"
            else -> "ROLE_VERIFICADOR"
        }
    }

    // Mejora Estrella 4: Diálogo de Verificación y Actualización Biométrica Facial / Huella AFIS
    state.ciudadanoParaBiometria?.let { ciudadanoBio ->
        DialogoVerificacionBiometrica(
            dni = ciudadanoBio.dni,
            nombreCiudadano = ciudadanoBio.nombreCompleto,
            onDismiss = { viewModel.abrirVerificacionBiometrica(null) },
            onVerificacionExitosa = { modo, puntaje ->
                versionBiometria++
                viewModel.confirmarBiometriaCiudadano(
                    usuario = sesion.usuario,
                    rol = rolUsuario,
                    modo = modo,
                    puntaje = puntaje
                )
            }
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
                        viewModel.eliminar(c.id, c.dni, sesion.usuario, rolUsuario)
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
                        text = "Padrón Nacional PIDE • ${formatearMiles(state.totalPadronNacional)} Ciudadanos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Base masiva en Supabase: DNIs del 40000001 al 40050000 + Cámara y Huella",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
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

        // Filtro Desplegable en Cascada por Departamento, Provincia y Distrito (Estilo Oficial RENIEC)
        item {
            SelectorUbigeoDesplegableReniec(
                tituloSeccion = "Lugar de Nacimiento / Domicilio (Ubigeo INEI) :",
                etiquetaDep = "Departamento de Nacimiento",
                etiquetaProv = "Provincia de Nacimiento",
                etiquetaDist = "Distrito de Nacimiento",
                departamentoActual = state.filtroDepartamento,
                provinciaActual = state.filtroProvincia,
                distritoActual = state.filtroDistrito,
                permitirTodos = true,
                onSeleccionarDepartamento = viewModel::onFiltroDepartamentoChange,
                onSeleccionarProvincia = viewModel::onFiltroProvinciaChange,
                onSeleccionarDistrito = { dist, _ -> viewModel.onFiltroDistritoChange(dist) }
            )
        }

        // Buscador por DNI, Nombre o Ubigeo en los +50,000 ciudadanos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.filtroDniONombre,
                    onValueChange = viewModel::onFiltroChange,
                    label = { Text("Buscar DNI (ej: 40012345), Apellidos o Distrito en +50K") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(
                    onClick = viewModel::consultarDniExacto,
                    modifier = Modifier.height(54.dp)
                ) {
                    Text("Buscar 50K")
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
                    versionBiometria = versionBiometria,
                    onCambio = viewModel::actualizarFormulario,
                    onAbrirEscanerModal = {
                        viewModel.abrirVerificacionBiometrica(
                            Ciudadano(
                                id = state.editandoId ?: 0L,
                                dni = state.formulario.dni.ifBlank { "TEMP" },
                                nombres = state.formulario.nombres.ifBlank { "CIUDADANO" },
                                apellidoPaterno = state.formulario.apellidoPaterno,
                                apellidoMaterno = state.formulario.apellidoMaterno,
                                fechaNacimiento = state.formulario.fechaNacimiento,
                                estadoCivil = state.formulario.estadoCivil,
                                ubigeo = state.formulario.ubigeo,
                                direccion = state.formulario.direccion,
                                donacionOrganos = state.formulario.donacionOrganos
                            )
                        )
                    },
                    onBiometriaCapturada = { modo, detalle ->
                        versionBiometria++
                        viewModel.registrarEventoTelemetriaOAuditoria(
                            usuario = sesion.usuario,
                            rol = rolUsuario,
                            accion = "CAPTURA_BIOMETRICA_FORMULARIO",
                            entidad = "BIOMETRIA",
                            referenciaDni = state.formulario.dni.ifBlank { "NUEVO_DNI" },
                            detalle = "$modo: $detalle"
                        )
                    },
                    onGuardar = { viewModel.guardarCiudadano(sesion.usuario, rolUsuario) },
                    onCancelar = viewModel::cerrarFormulario
                )
            }
        }

        // Mejora Estrella 2: DNIe 3.0 Interactivo con Foto Real de Cámara, QR 2D, Huella AFIS y MRZ
        state.ciudadanoSeleccionado?.let { seleccionado ->
            item {
                TarjetaDniElectronicoInteractivo(
                    ciudadano = seleccionado,
                    versionBiometria = versionBiometria,
                    puedeCrearTramite = puedeCrearTramite,
                    puedeEditar = puedeEscribirPadron,
                    onIniciarTramite = { onIniciarTramiteParaCiudadano(seleccionado) },
                    onValidarBiometria = { viewModel.abrirVerificacionBiometrica(seleccionado) },
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
                    text = "Resultados en ${if (state.filtroDepartamento == "TODOS") "Perú (+50,000 en nube)" else state.filtroDepartamento} (${state.ciudadanosFiltrados.size} mostrados)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (state.filtroDniONombre.isNotBlank()) {
                    TextButton(onClick = { viewModel.onFiltroChange("") }) {
                        Text("Limpiar filtro", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(state.ciudadanosFiltrados, key = { "${it.dni}-${it.id}" }) { ciudadano ->
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
                        IconButton(onClick = { viewModel.abrirVerificacionBiometrica(ciudadano) }) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = "Escanear / Actualizar Rostro y Huella",
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
    versionBiometria: Int,
    puedeCrearTramite: Boolean,
    puedeEditar: Boolean,
    onIniciarTramite: () -> Unit,
    onValidarBiometria: () -> Unit,
    onEditar: () -> Unit
) {
    var mostrarReverso by remember(ciudadano.dni) { mutableStateOf(false) }
    val digitoVerificador = remember(ciudadano.dni) {
        (ciudadano.dni.sumOf { it.digitToIntOrNull() ?: 0 } % 9) + 1
    }
    val ubigeoCatalogo = remember(ciudadano.ubigeo) {
        CatalogoUbigeoReniec.buscarUbigeo(ciudadano.ubigeo)
    }
    val fotoCapturada = remember(ciudadano.dni, versionBiometria) {
        BiometriaSesionCache.fotosPorDni[ciudadano.dni] ?: BiometriaSesionCache.fotosPorDni["TEMP"]
    }
    val estadoFacial = remember(ciudadano.dni, versionBiometria) {
        BiometriaSesionCache.estadoFacialPorDni[ciudadano.dni] ?: "AFIS 99.6% Verificado"
    }
    val estadoHuella = remember(ciudadano.dni, versionBiometria) {
        BiometriaSesionCache.estadoHuellaPorDni[ciudadano.dni] ?: "Índice Der. Registrado"
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
                            "DOCUMENTO NACIONAL DE IDENTIDAD ELECTRÓNICO 3.0 (ANVERSO)"
                        } else {
                            "DNIe 3.0 • QR VERIFICABLE, HUELLA AFIS Y ZONA MRZ (REVERSO)"
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
                                if (fotoCapturada != null) {
                                    Image(
                                        bitmap = fotoCapturada,
                                        contentDescription = "Fotografía Biométrica DNIe",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(46.dp)
                                    )
                                }
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
                                    text = "CHIP PKI",
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
                        Text(
                            text = "ROSTRO: $estadoFacial",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "HUELLA: $estadoHuella",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
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

                    // Huella Dactilar Índice Derecho
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(66.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            LienzoHuellaSimulada(
                                color = MaterialTheme.colorScheme.primary,
                                tamano = 42.dp
                            )
                            Text(
                                text = "AFIS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Código QR 2D Verificable
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        modifier = Modifier.size(66.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(5.dp)) {
                            LienzoQrSimulado(
                                semilla = "${ciudadano.dni}-${ciudadano.apellidoPaterno}",
                                tamano = 56.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
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
                        text = if (!mostrarReverso) "Toca la tarjeta para girar el DNIe 3.0 (Ver QR + Huella + MRZ)" else "Toca la tarjeta para volver al Anverso",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledTonalButton(
                    onClick = onValidarBiometria,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rostro/Huella", style = MaterialTheme.typography.labelMedium)
                }
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
private fun FormularioCiudadanoCard(
    esEdicion: Boolean,
    form: FormularioCiudadano,
    guardando: Boolean,
    versionBiometria: Int,
    onCambio: ((FormularioCiudadano) -> FormularioCiudadano) -> Unit,
    onAbrirEscanerModal: () -> Unit,
    onBiometriaCapturada: (modo: String, detalle: String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    val claveDni = form.dni.ifBlank { "TEMP" }
    var estadoRostroLocal by remember(claveDni, versionBiometria) {
        mutableStateOf(BiometriaSesionCache.estadoFacialPorDni[claveDni] ?: if (esEdicion) "Registrado AFIS 99.6%" else "Pendiente captura")
    }
    var estadoHuellaLocal by remember(claveDni, versionBiometria) {
        mutableStateOf(BiometriaSesionCache.estadoHuellaPorDni[claveDni] ?: if (esEdicion) "Huella Índice Der. OK" else "Pendiente captura")
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (esEdicion) "Editar Ficha y Biometría Ciudadana RENIEC" else "Empadronar Nuevo Ciudadano (Con Rostro y Huella)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Captura / Actualización de Rostro (Cámara Real) y Huella (Sensor) dentro del formulario
            TarjetaCapturaBiometricaFormulario(
                dni = form.dni,
                estadoFacial = estadoRostroLocal,
                estadoHuella = estadoHuellaLocal,
                onRostroActualizado = { resumen, bmp ->
                    estadoRostroLocal = resumen
                    if (form.dni.isNotBlank() && bmp != null) {
                        BiometriaSesionCache.fotosPorDni[form.dni] = bmp
                    }
                    onBiometriaCapturada("ROSTRO", resumen)
                },
                onHuellaActualizada = { resumen ->
                    estadoHuellaLocal = resumen
                    onBiometriaCapturada("HUELLA", resumen)
                },
                onAbrirEscanerCompleto = onAbrirEscanerModal
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

            var depForm by remember(form.ubigeo) {
                mutableStateOf(CatalogoUbigeoReniec.detectarDepartamento(form.ubigeo))
            }
            var provForm by remember(form.ubigeo) {
                mutableStateOf(CatalogoUbigeoReniec.detectarProvincia(form.ubigeo))
            }
            var distForm by remember(form.ubigeo) {
                mutableStateOf(CatalogoUbigeoReniec.detectarDistrito(form.ubigeo))
            }

            SelectorUbigeoDesplegableReniec(
                tituloSeccion = "Lugar de Nacimiento / Domicilio :",
                etiquetaDep = "Departamento de Nacimiento",
                etiquetaProv = "Provincia de Nacimiento",
                etiquetaDist = "Distrito de Nacimiento",
                departamentoActual = depForm,
                provinciaActual = provForm,
                distritoActual = distForm,
                permitirTodos = false,
                onSeleccionarDepartamento = { nuevoDep ->
                    depForm = nuevoDep
                    val primeraSede = CatalogoUbigeoReniec.distritosPorProvincia(nuevoDep, "TODAS").firstOrNull()
                    if (primeraSede != null) {
                        provForm = primeraSede.provincia
                        distForm = primeraSede.distrito
                        onCambio { it.copy(ubigeo = primeraSede.etiquetaCompleta, ubigeoError = null) }
                    }
                },
                onSeleccionarProvincia = { nuevaProv ->
                    provForm = nuevaProv
                    val primeraSede = CatalogoUbigeoReniec.distritosPorProvincia(depForm, nuevaProv).firstOrNull()
                    if (primeraSede != null) {
                        distForm = primeraSede.distrito
                        onCambio { it.copy(ubigeo = primeraSede.etiquetaCompleta, ubigeoError = null) }
                    }
                },
                onSeleccionarDistrito = { nuevoDist, sede ->
                    distForm = nuevoDist
                    if (sede != null) {
                        onCambio { it.copy(ubigeo = sede.etiquetaCompleta, ubigeoError = null) }
                    }
                }
            )

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
                    onClick = {
                        // Si se capturó como TEMP antes de escribir el DNI, asociarlo al DNI final
                        if (form.dni.length == 8) {
                            BiometriaSesionCache.fotosPorDni["TEMP"]?.let {
                                BiometriaSesionCache.fotosPorDni[form.dni] = it
                            }
                            BiometriaSesionCache.estadoFacialPorDni["TEMP"]?.let {
                                BiometriaSesionCache.estadoFacialPorDni[form.dni] = it
                            }
                            BiometriaSesionCache.estadoHuellaPorDni["TEMP"]?.let {
                                BiometriaSesionCache.estadoHuellaPorDni[form.dni] = it
                            }
                        }
                        onGuardar()
                    },
                    enabled = !guardando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (esEdicion) "Guardar Cambios y Biometría" else "Registrar con Biometría")
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
