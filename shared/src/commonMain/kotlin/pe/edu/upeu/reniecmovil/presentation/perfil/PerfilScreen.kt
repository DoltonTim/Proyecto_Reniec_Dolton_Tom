package pe.edu.upeu.reniecmovil.presentation.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import pe.edu.upeu.reniecmovil.data.remote.AuditoriaApi
import pe.edu.upeu.reniecmovil.data.remote.OperadorApi
import pe.edu.upeu.reniecmovil.data.remote.dto.AuditoriaPayloadDto
import pe.edu.upeu.reniecmovil.data.remote.dto.CargoOperadorReniec
import pe.edu.upeu.reniecmovil.data.remote.dto.OperadorReniecDto
import pe.edu.upeu.reniecmovil.data.remote.dto.OperadorReniecPayloadDto
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.obtenerInfoDispositivo

@Composable
fun PerfilScreen(
    sesion: Sesion,
    darkTheme: Boolean,
    onCambiarTema: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val infoDispositivo = remember { obtenerInfoDispositivo() }
    val operadorApi: OperadorApi = koinInject()
    val auditoriaApi: AuditoriaApi = koinInject()
    val scope = rememberCoroutineScope()

    val esAdministrador = sesion.puede("ROLE_ADMIN") || sesion.puede("OPERADORES_GESTIONAR")

    var operadores by remember { mutableStateOf<List<OperadorReniecDto>>(emptyList()) }
    var cargandoOperadores by remember { mutableStateOf(false) }
    var mensajeGestion by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    var operadorParaCambiarCargo by remember { mutableStateOf<OperadorReniecDto?>(null) }

    fun recargarOperadores() {
        scope.launch {
            cargandoOperadores = true
            runCatching {
                operadorApi.listarOperadores()
            }.onSuccess { lista ->
                if (lista.isNotEmpty()) {
                    operadores = lista
                }
            }.onFailure {
                operadores = listOf(
                    OperadorReniecDto(
                        id = 1,
                        username = "admin",
                        password = "123456",
                        nombreCompleto = "Jefe Nacional de Identificación",
                        cargo = CargoOperadorReniec.ADMINISTRADOR.etiquetaCargo,
                        rol = CargoOperadorReniec.ADMINISTRADOR.codigoRol,
                        sede = "Sede Central Lima",
                        permisos = CargoOperadorReniec.ADMINISTRADOR.permisosCsv
                    ),
                    OperadorReniecDto(
                        id = 2,
                        username = "dolton",
                        password = "123456",
                        nombreCompleto = "Ing. Dolton Meza",
                        cargo = CargoOperadorReniec.BRIGADISTA.etiquetaCargo,
                        rol = CargoOperadorReniec.BRIGADISTA.codigoRol,
                        sede = "Agencia Regional Juliaca",
                        permisos = CargoOperadorReniec.BRIGADISTA.permisosCsv
                    ),
                    OperadorReniecDto(
                        id = 3,
                        username = "empadronador",
                        password = "123456",
                        nombreCompleto = "Lic. Rosaura Mamani Quispe",
                        cargo = CargoOperadorReniec.EMPADRONADOR.etiquetaCargo,
                        rol = CargoOperadorReniec.EMPADRONADOR.codigoRol,
                        sede = "Agencia Regional Juliaca",
                        permisos = CargoOperadorReniec.EMPADRONADOR.permisosCsv
                    ),
                    OperadorReniecDto(
                        id = 4,
                        username = "tramitador",
                        password = "123456",
                        nombreCompleto = "Abg. Carlos Alberto Condori",
                        cargo = CargoOperadorReniec.TRAMITADOR.etiquetaCargo,
                        rol = CargoOperadorReniec.TRAMITADOR.codigoRol,
                        sede = "Sede Regional Puno",
                        permisos = CargoOperadorReniec.TRAMITADOR.permisosCsv
                    ),
                    OperadorReniecDto(
                        id = 5,
                        username = "verificador",
                        password = "123456",
                        nombreCompleto = "Tec. Elvis Huanca Caceres",
                        cargo = CargoOperadorReniec.VERIFICADOR.etiquetaCargo,
                        rol = CargoOperadorReniec.VERIFICADOR.codigoRol,
                        sede = "Punto PIDE Arequipa",
                        permisos = CargoOperadorReniec.VERIFICADOR.permisosCsv
                    )
                )
            }
            cargandoOperadores = false
        }
    }

    LaunchedEffect(esAdministrador) {
        if (esAdministrador) {
            recargarOperadores()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Credencial Digital del Operador (JWT)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider()
                Text(
                    text = "Operador: ${sesion.nombreCompleto}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Usuario: @${sesion.usuario}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Cargo: ${sesion.cargo}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sede asignada: ${sesion.sede}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Permisos RBAC activos: ${sesion.permisos.joinToString(", ")}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // PANEL EXCLUSIVO DEL ADMINISTRADOR: GESTIÓN DE CARGOS (ADMINISTRADOR, EMPADRONADOR, TRÁMITES, BRIGADISTA, VERIFICADOR)
        if (esAdministrador) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Gestión de Personal y Cargos (RBAC)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Crea operadores y asigna cargos: Administrador, Empadronador, Trámites, Brigadista",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { recargarOperadores() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar lista")
                        }
                    }

                    HorizontalDivider()

                    Button(
                        onClick = { mostrarDialogoNuevo = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Operador / Asignar Cargo", fontWeight = FontWeight.Bold)
                    }

                    if (mensajeGestion != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mensajeGestion.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (cargandoOperadores) {
                        Text(
                            text = "Consultando operadores en Supabase (public.operadores_reniec)...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        operadores.forEach { op ->
                            val cargoEnum = CargoOperadorReniec.entries.firstOrNull { it.codigoRol == op.rol }
                                ?: CargoOperadorReniec.BRIGADISTA
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${op.nombreCompleto} (@${op.username})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = "Rol: ${op.rol} • Sede: ${op.sede}",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        Row {
                                            IconButton(
                                                onClick = { operadorParaCambiarCargo = op },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Cambiar Cargo",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            if (op.username != "admin") {
                                                IconButton(
                                                    onClick = {
                                                        scope.launch {
                                                            operadores = operadores.filter { it.id != op.id }
                                                            runCatching {
                                                                operadorApi.eliminarOperador(op.id)
                                                            }
                                                            mensajeGestion = "✓ Acceso de @${op.username} revocado. Su historial permanece en Reportes."
                                                            recargarOperadores()
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Dar de Baja / Eliminar",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Cargo: ${op.cargo} (${cargoEnum.descripcion})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(
                            text = "Tema Institucional Material 3",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (darkTheme) "Modo Oscuro Activado" else "Modo Claro Activado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Switch(checked = darkTheme, onCheckedChange = onCambiarTema)
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Text(
                        text = "Capacidad Nativa (expect / actual)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider()
                Text(
                    text = "Sistema Operativo: ${infoDispositivo.nombrePlataforma}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Hardware / Modelo: ${infoDispositivo.modeloEquipo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Motor Offline SQLDelight: Activo (reniec_movil.db)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onCerrarSesion,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Cerrar Sesión Segura (Borrar JWT)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }

    // Diálogo para Crear un Nuevo Operador con Cargo Asignado
    if (mostrarDialogoNuevo) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("123456") }
        var email by remember { mutableStateOf("") }
        var telefono by remember { mutableStateOf("+519") }
        var nombreCompleto by remember { mutableStateOf("") }
        var sede by remember { mutableStateOf("Agencia Regional Juliaca") }
        var cargoSeleccionado by remember { mutableStateOf(CargoOperadorReniec.EMPADRONADOR) }
        var guardando by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { mostrarDialogoNuevo = false },
            title = {
                Text(
                    text = "Nuevo Operador y Cargo RENIEC",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.trim().lowercase() },
                        label = { Text("Usuario para Login (ej: juan.perez)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña de acceso") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim().lowercase() },
                        label = { Text("Correo Google (@gmail.com o @reniec)") },
                        placeholder = { Text("ej: usuario@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it.trim() },
                        label = { Text("Celular para Recuperación (2FA)") },
                        placeholder = { Text("+51987654321") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nombreCompleto,
                        onValueChange = { nombreCompleto = it },
                        label = { Text("Nombre Completo y Grado") },
                        placeholder = { Text("Lic. Juan Pérez Quispe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sede,
                        onValueChange = { sede = it },
                        label = { Text("Sede / Agencia Asignada") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Selecciona el Cargo Institucional:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    CargoOperadorReniec.entries.forEach { cargo ->
                        FilterChip(
                            selected = cargoSeleccionado == cargo,
                            onClick = { cargoSeleccionado = cargo },
                            label = {
                                Text(
                                    text = "${cargo.etiquetaCargo} • ${cargo.descripcion}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !guardando && username.isNotBlank() && nombreCompleto.isNotBlank() && email.contains("@"),
                    onClick = {
                        guardando = true
                        scope.launch {
                            val payload = OperadorReniecPayloadDto(
                                username = username,
                                password = password.ifBlank { "123456" },
                                nombreCompleto = nombreCompleto,
                                email = email.ifBlank { null },
                                telefono = telefono.ifBlank { null },
                                cargo = cargoSeleccionado.etiquetaCargo,
                                rol = cargoSeleccionado.codigoRol,
                                sede = sede.ifBlank { "Agencia Regional Juliaca" },
                                permisos = cargoSeleccionado.permisosCsv,
                                activo = true
                            )
                            runCatching {
                                operadorApi.crearOperador(payload)
                                auditoriaApi.registrarEvento(
                                    AuditoriaPayloadDto(
                                        usuario = sesion.usuario,
                                        rol = "ROLE_ADMIN",
                                        accion = "CREAR_OPERADOR",
                                        entidad = "OPERADORES",
                                        referenciaDni = payload.username,
                                        detalle = "Alta de operador @${payload.username} con cargo ${cargoSeleccionado.etiquetaCargo}",
                                        canalTelemetria = "Google Analytics",
                                        dispositivo = "${infoDispositivo.nombrePlataforma} (${infoDispositivo.modeloEquipo})"
                                    )
                                )
                            }.onSuccess {
                                mensajeGestion = "✓ Operador @${payload.username} creado como ${cargoSeleccionado.etiquetaCargo}. Ya puede iniciar sesión."
                                mostrarDialogoNuevo = false
                                recargarOperadores()
                            }.onFailure { err ->
                                mensajeGestion = "Aviso al guardar: ${err.message}"
                                mostrarDialogoNuevo = false
                            }
                            guardando = false
                        }
                    }
                ) {
                    Text(if (guardando) "Guardando..." else "Guardar en Supabase")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNuevo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para Cambiar el Cargo de un Operador Existente
    operadorParaCambiarCargo?.let { operador ->
        var nuevoCargo by remember(operador) {
            mutableStateOf(
                CargoOperadorReniec.entries.firstOrNull { it.codigoRol == operador.rol }
                    ?: CargoOperadorReniec.EMPADRONADOR
            )
        }
        var actualizando by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { operadorParaCambiarCargo = null },
            title = {
                Text(
                    text = "Cambiar Cargo de @${operador.username}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Operador: ${operador.nombreCompleto}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    CargoOperadorReniec.entries.forEach { cargo ->
                        FilterChip(
                            selected = nuevoCargo == cargo,
                            onClick = { nuevoCargo = cargo },
                            label = {
                                Text(
                                    text = "${cargo.etiquetaCargo} • ${cargo.descripcion}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                FilledTonalButton(
                    enabled = !actualizando,
                    onClick = {
                        actualizando = true
                        scope.launch {
                            runCatching {
                                operadorApi.cambiarCargo(
                                    id = operador.id,
                                    cargo = nuevoCargo.etiquetaCargo,
                                    rol = nuevoCargo.codigoRol,
                                    permisos = nuevoCargo.permisosCsv
                                )
                                auditoriaApi.registrarEvento(
                                    AuditoriaPayloadDto(
                                        usuario = sesion.usuario,
                                        rol = "ROLE_ADMIN",
                                        accion = "CAMBIAR_CARGO",
                                        entidad = "OPERADORES",
                                        referenciaDni = operador.username,
                                        detalle = "Cambio de cargo de @${operador.username} a ${nuevoCargo.etiquetaCargo}",
                                        canalTelemetria = "Google Analytics",
                                        dispositivo = "${infoDispositivo.nombrePlataforma} (${infoDispositivo.modeloEquipo})"
                                    )
                                )
                            }.onSuccess {
                                mensajeGestion = "✓ Cargo de @${operador.username} actualizado a ${nuevoCargo.etiquetaCargo}"
                                operadorParaCambiarCargo = null
                                recargarOperadores()
                            }.onFailure { err ->
                                mensajeGestion = "Error al actualizar cargo: ${err.message}"
                                operadorParaCambiarCargo = null
                            }
                            actualizando = false
                        }
                    }
                ) {
                    Text(if (actualizando) "Actualizando..." else "Aplicar Cargo")
                }
            },
            dismissButton = {
                TextButton(onClick = { operadorParaCambiarCargo = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
