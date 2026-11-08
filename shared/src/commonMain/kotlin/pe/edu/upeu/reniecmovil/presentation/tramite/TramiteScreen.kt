package pe.edu.upeu.reniecmovil.presentation.tramite

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.reniecmovil.BiometriaSesionCache
import pe.edu.upeu.reniecmovil.domain.model.CatalogoUbigeoReniec
import pe.edu.upeu.reniecmovil.domain.model.EstadoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.domain.model.TipoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni
import pe.edu.upeu.reniecmovil.presentation.componentes.DialogoVerificacionBiometrica
import pe.edu.upeu.reniecmovil.presentation.componentes.DialogoVoucherTramiteQr
import pe.edu.upeu.reniecmovil.presentation.componentes.SelectorUbigeoDesplegableReniec
import pe.edu.upeu.reniecmovil.presentation.componentes.TarjetaCapturaBiometricaFormulario

@Composable
fun TramiteScreen(
    viewModel: TramiteViewModel,
    sesion: Sesion
) {
    val state by viewModel.uiState.collectAsState()
    var tramiteParaEliminar by remember { mutableStateOf<TramiteDni?>(null) }
    var filtroDepartamento by remember { mutableStateOf("TODOS") }
    var filtroProvincia by remember { mutableStateOf("TODAS") }
    var filtroDistrito by remember { mutableStateOf("TODOS") }

    val puedeEscribirTramite = sesion.puede("TRAMITES_ESCRIBIR")
    val puedeAprobarTramite = sesion.puede("TRAMITES_APROBAR") || sesion.puede("ROLE_ADMIN")
    val puedeEliminarTramite = sesion.puede("TRAMITES_ELIMINAR") || sesion.puede("ROLE_ADMIN")
    val rolUsuario = remember(sesion) {
        when {
            sesion.puede("ROLE_ADMIN") -> "ROLE_ADMIN"
            sesion.puede("ROLE_BRIGADISTA") -> "ROLE_BRIGADISTA"
            else -> "ROLE_VERIFICADOR"
        }
    }

    val tramitesFiltradosPorUbigeo = remember(state.tramitesFiltrados, filtroDepartamento, filtroProvincia, filtroDistrito) {
        state.tramitesFiltrados.filter { t ->
            val okDep = filtroDepartamento == "TODOS" ||
                CatalogoUbigeoReniec.detectarDepartamento(t.ubigeoDestino).equals(filtroDepartamento, ignoreCase = true) ||
                t.ubigeoDestino.contains(filtroDepartamento, ignoreCase = true)
            val okProv = filtroProvincia == "TODAS" ||
                t.ubigeoDestino.contains(filtroProvincia, ignoreCase = true)
            val okDist = filtroDistrito == "TODOS" ||
                t.ubigeoDestino.contains(filtroDistrito, ignoreCase = true)
            okDep && okProv && okDist
        }
    }

    // Mejora Estrella 3: Voucher / Constancia Oficial de Trámite con QR (Listo para Compartir)
    state.tramiteVoucherActivo?.let { tramiteVoucher ->
        DialogoVoucherTramiteQr(
            tramite = tramiteVoucher,
            onDismiss = { viewModel.abrirVoucherQr(null) },
            onSolicitarBiometria = {
                viewModel.abrirVoucherQr(null)
                viewModel.abrirBiometriaTramite(tramiteVoucher)
            },
            onRegistrarEventoCompartir = {
                viewModel.registrarAuditoria(
                    usuario = sesion.usuario,
                    rol = rolUsuario,
                    accion = "COMPARTIR_VOUCHER_QR",
                    referenciaDni = tramiteVoucher.dniCiudadano,
                    detalle = "Voucher QR copiado/compartido para ${tramiteVoucher.nombreCiudadano}",
                    canal = "Google Analytics + Píxel de Meta + Clarity"
                )
            }
        )
    }

    // Mejora Estrella 4: Verificación Biométrica Facial / Huella antes de aprobar o registrar
    state.tramiteBiometriaActivo?.let { tramiteBio ->
        DialogoVerificacionBiometrica(
            dni = tramiteBio.dniCiudadano,
            nombreCiudadano = tramiteBio.nombreCiudadano,
            onDismiss = { viewModel.abrirBiometriaTramite(null) },
            onVerificacionExitosa = { modo, puntaje ->
                viewModel.confirmarBiometriaYAprobar(
                    usuario = sesion.usuario,
                    rol = rolUsuario,
                    modo = modo,
                    puntaje = puntaje
                )
            }
        )
    }

    tramiteParaEliminar?.let { tramite ->
        AlertDialog(
            onDismissRequest = { tramiteParaEliminar = null },
            title = { Text("Confirmar Anulación de Trámite DNI") },
            text = {
                Text(
                    "¿Desea anular el trámite de ${tramite.nombreCiudadano} (DNI ${tramite.dniCiudadano})? Esta acción se auditará en Central RENIEC."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminar(tramite.id, tramite.dniCiudadano, sesion.usuario, rolUsuario)
                        tramiteParaEliminar = null
                    }
                ) {
                    Text("Anular Trámite")
                }
            },
            dismissButton = {
                TextButton(onClick = { tramiteParaEliminar = null }) {
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
                        text = "Trámites DNIe • Voucher QR + Rostro y Huella",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Autocompletado desde +50,000 ciudadanos y captura biométrica",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
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
                    if (puedeEscribirTramite) {
                        Button(onClick = { viewModel.abrirNuevoTramite() }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Nuevo")
                        }
                    }
                }
            }
        }

        // Filtro Desplegable en Cascada por Departamento, Provincia y Distrito de Atención
        item {
            SelectorUbigeoDesplegableReniec(
                tituloSeccion = "Sede Regional / Ubicación de Trámite (${tramitesFiltradosPorUbigeo.size} trámites) :",
                etiquetaDep = "Departamento de Atención",
                etiquetaProv = "Provincia de Atención",
                etiquetaDist = "Distrito / Agencia",
                departamentoActual = filtroDepartamento,
                provinciaActual = filtroProvincia,
                distritoActual = filtroDistrito,
                permitirTodos = true,
                onSeleccionarDepartamento = { dep ->
                    filtroDepartamento = dep
                    filtroProvincia = "TODAS"
                    filtroDistrito = "TODOS"
                },
                onSeleccionarProvincia = { prov ->
                    filtroProvincia = prov
                    filtroDistrito = "TODOS"
                },
                onSeleccionarDistrito = { dist, _ ->
                    filtroDistrito = dist
                }
            )
        }

        item {
            OutlinedTextField(
                value = state.filtroBusqueda,
                onValueChange = viewModel::onFiltroBusquedaChange,
                label = { Text("Filtrar por DNI, Ciudadano, Tipo o Distrito") },
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

        if (state.mostrarFormulario && puedeEscribirTramite) {
            item {
                FormularioTramiteCard(
                    esEdicion = state.editandoId != null,
                    form = state.formulario,
                    guardando = state.guardando,
                    buscandoDni = state.buscandoDniPadron,
                    onCambio = viewModel::actualizarFormulario,
                    onBuscarDni50k = { viewModel.autocompletarDesdePadron50k(state.formulario.dniCiudadano) },
                    onAbrirEscanerModal = {
                        viewModel.abrirBiometriaTramite(
                            TramiteDni(
                                id = state.editandoId ?: 0L,
                                dniCiudadano = state.formulario.dniCiudadano.ifBlank { "TEMP" },
                                nombreCiudadano = state.formulario.nombreCiudadano.ifBlank { "SOLICITANTE" },
                                tipoTramite = state.formulario.tipoTramite,
                                estado = state.formulario.estado,
                                ubigeoDestino = state.formulario.ubigeoDestino,
                                observaciones = state.formulario.observaciones
                            )
                        )
                    },
                    onGuardar = { viewModel.guardar(sesion.usuario, rolUsuario) },
                    onCancelar = viewModel::cerrarFormulario
                )
            }
        }

        items(tramitesFiltradosPorUbigeo, key = { it.id }) { tramite ->
            TarjetaTramiteItem(
                tramite = tramite,
                puedeEditar = puedeEscribirTramite,
                puedeAprobar = puedeAprobarTramite,
                puedeEliminar = puedeEliminarTramite,
                onVerVoucherQr = { viewModel.abrirVoucherQr(tramite) },
                onValidarBiometria = { viewModel.abrirBiometriaTramite(tramite) },
                onEditar = { viewModel.iniciarEdicion(tramite) },
                onMarcarAtendido = { viewModel.cambiarEstadoRapido(tramite, EstadoTramiteDni.ATENDIDO, sesion.usuario, rolUsuario) },
                onEliminar = { tramiteParaEliminar = tramite }
            )
        }
    }
}

@Composable
private fun TarjetaTramiteItem(
    tramite: TramiteDni,
    puedeEditar: Boolean,
    puedeAprobar: Boolean,
    puedeEliminar: Boolean,
    onVerVoucherQr: () -> Unit,
    onValidarBiometria: () -> Unit,
    onEditar: () -> Unit,
    onMarcarAtendido: () -> Unit,
    onEliminar: () -> Unit
) {
    val tipo = TipoTramiteDni.desdeTexto(tramite.tipoTramite)
    val estado = EstadoTramiteDni.desdeTexto(tramite.estado)
    val esOffline = tramite.pendienteSync || tramite.id < 0
    val infoUbigeo = remember(tramite.ubigeoDestino) {
        CatalogoUbigeoReniec.buscarUbigeo(tramite.ubigeoDestino)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DNI ${tramite.dniCiudadano} • ${tramite.nombreCiudadano}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
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

            StepperEstadoTramite(estado = estado)

            Text(
                text = "Ubigeo (${infoUbigeo?.codigo ?: "150101"}): ${tramite.ubigeoDestino} | Fecha: ${tramite.fechaRegistro}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Observación: ${tramite.observaciones}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(onClick = onVerVoucherQr) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Voucher QR", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(onClick = onValidarBiometria) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rostro/Huella", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Row {
                    if (puedeAprobar && estado != EstadoTramiteDni.ATENDIDO) {
                        IconButton(onClick = onMarcarAtendido) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Aprobar / Marcar Atendido",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    if (puedeEditar) {
                        IconButton(onClick = onEditar) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Trámite")
                        }
                    }
                    if (puedeEliminar) {
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
    }
}

@Composable
private fun StepperEstadoTramite(estado: EstadoTramiteDni) {
    val nivelActual = when (estado) {
        EstadoTramiteDni.PENDIENTE -> 1
        EstadoTramiteDni.OBSERVADO -> 2
        EstadoTramiteDni.EN_PROCESO -> 2
        EstadoTramiteDni.ATENDIDO -> 3
    }
    val pasos = listOf("1. Recibido", "2. Procesando", "3. Entregado")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        pasos.forEachIndexed { idx, etiqueta ->
            val activo = (idx + 1) <= nivelActual
            Surface(
                color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (activo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal
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
    buscandoDni: Boolean,
    onCambio: ((FormularioTramite) -> FormularioTramite) -> Unit,
    onBuscarDni50k: () -> Unit,
    onAbrirEscanerModal: () -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    val claveDni = form.dniCiudadano.ifBlank { "TEMP" }
    var estadoRostroLocal by remember(claveDni) {
        mutableStateOf(BiometriaSesionCache.estadoFacialPorDni[claveDni] ?: "AFIS 99.6% Activo")
    }
    var estadoHuellaLocal by remember(claveDni) {
        mutableStateOf(BiometriaSesionCache.estadoHuellaPorDni[claveDni] ?: "Índice Der. Activo")
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
                text = if (esEdicion) "Actualizar Trámite y Biometría de DNI" else "Registrar Solicitud de Trámite DNI (+50K)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            TarjetaCapturaBiometricaFormulario(
                dni = form.dniCiudadano,
                estadoFacial = estadoRostroLocal,
                estadoHuella = estadoHuellaLocal,
                onRostroActualizado = { resumen, _ ->
                    estadoRostroLocal = resumen
                    onCambio {
                        val obsBase = it.observaciones.ifBlank { "Trámite con validación biométrica" }
                        it.copy(observaciones = "$obsBase | Rostro: $resumen", observacionesError = null)
                    }
                },
                onHuellaActualizada = { resumen ->
                    estadoHuellaLocal = resumen
                    onCambio {
                        val obsBase = it.observaciones.ifBlank { "Trámite con validación biométrica" }
                        it.copy(observaciones = "$obsBase | Huella: $resumen", observacionesError = null)
                    }
                },
                onAbrirEscanerCompleto = onAbrirEscanerModal
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = form.dniCiudadano,
                    onValueChange = { v -> onCambio { it.copy(dniCiudadano = v.take(8), dniError = null) } },
                    label = { Text("DNI (ej: 40000001 al 40050000)") },
                    isError = form.dniError != null,
                    supportingText = form.dniError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(
                    onClick = onBuscarDni50k,
                    enabled = !buscandoDni,
                    modifier = Modifier.height(54.dp)
                ) {
                    Text(if (buscandoDni) "..." else "Buscar 50K")
                }
            }

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

            var depForm by remember(form.ubigeoDestino) {
                mutableStateOf(CatalogoUbigeoReniec.detectarDepartamento(form.ubigeoDestino))
            }
            var provForm by remember(form.ubigeoDestino) {
                mutableStateOf(CatalogoUbigeoReniec.detectarProvincia(form.ubigeoDestino))
            }
            var distForm by remember(form.ubigeoDestino) {
                mutableStateOf(CatalogoUbigeoReniec.detectarDistrito(form.ubigeoDestino))
            }

            SelectorUbigeoDesplegableReniec(
                tituloSeccion = "Sede / Agencia de Trámite :",
                etiquetaDep = "Departamento de Trámite",
                etiquetaProv = "Provincia de Trámite",
                etiquetaDist = "Distrito / Sede",
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
                        onCambio {
                            it.copy(
                                ubigeoDestino = "${primeraSede.etiquetaCompleta} (${primeraSede.agenciaReniec})",
                                ubigeoError = null
                            )
                        }
                    }
                },
                onSeleccionarProvincia = { nuevaProv ->
                    provForm = nuevaProv
                    val primeraSede = CatalogoUbigeoReniec.distritosPorProvincia(depForm, nuevaProv).firstOrNull()
                    if (primeraSede != null) {
                        distForm = primeraSede.distrito
                        onCambio {
                            it.copy(
                                ubigeoDestino = "${primeraSede.etiquetaCompleta} (${primeraSede.agenciaReniec})",
                                ubigeoError = null
                            )
                        }
                    }
                },
                onSeleccionarDistrito = { nuevoDist, sede ->
                    distForm = nuevoDist
                    if (sede != null) {
                        onCambio {
                            it.copy(
                                ubigeoDestino = "${sede.etiquetaCompleta} (${sede.agenciaReniec})",
                                ubigeoError = null
                            )
                        }
                    }
                }
            )

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
                    Text(if (esEdicion) "Actualizar Trámite" else "Emitir con Voucher QR")
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
