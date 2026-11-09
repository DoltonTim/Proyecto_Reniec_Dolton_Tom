package pe.edu.upeu.reniecmovil.presentation.sesion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import reniecmovil.shared.generated.resources.Res
import reniecmovil.shared.generated.resources.mascota_reniec
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    viewModel: SesionViewModel,
    onLoginExitoso: () -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()
    var mostrarClave by remember { mutableStateOf(false) }
    var mostrarDialogoGoogle by remember { mutableStateOf(false) }
    var correoRecuperacion by remember { mutableStateOf("doltontomsistemas@gmail.com") }
    var telefonoRecuperacion by remember { mutableStateOf("+51930703759") }
    var nuevaClaveRecuperacion by remember { mutableStateOf("") }
    var codigoOtp by remember { mutableStateOf("") }
    var dialogoAyuda by remember { mutableStateOf<String?>(null) }

    val colorHeaderTop = Color(0xFF8C0B42)
    val colorHeaderBottom = Color(0xFF640D5F)
    val colorDarkSurface = Color(0xFF1A0926)
    val colorLimeAccent = Color(0xFFC6FF00)
    val colorTextSecondary = Color(0xFFBBA8C9)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorHeaderTop, colorHeaderBottom, colorDarkSurface)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val peakY = h * 0.23f
            val sideY = h * 0.39f
            val radius = 75f

            val path = Path().apply {
                moveTo(0f, sideY)
                lineTo(w * 0.5f - radius, peakY + radius * 0.55f)
                quadraticTo(
                    w * 0.5f, peakY,
                    w * 0.5f + radius, peakY + radius * 0.55f
                )
                lineTo(w, sideY)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = path, color = colorDarkSurface)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .rotate(45f)
                        .border(2.5.dp, Color.White, RoundedCornerShape(9.dp))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.5.dp, colorLimeAccent, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "Emblema RENIEC",
                            tint = Color.White,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(-45f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column {
                    Text(
                        text = "Reniec Móvil",
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp
                    )
                    Text(
                        text = "Red Nacional de Identificación",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF1A0B2E),
                border = BorderStroke(2.dp, colorLimeAccent),
                modifier = Modifier.size(148.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.mascota_reniec),
                    contentDescription = "Mascota Oficial 3D RENIEC Móvil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Login",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(34.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Usuario",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
                TextField(
                    value = estado.usuario,
                    onValueChange = viewModel::onUsuarioChange,
                    placeholder = {
                        Text(
                            text = "ej: admin, empadronador, tramitador, dolton",
                            color = colorTextSecondary.copy(alpha = 0.65f),
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = colorLimeAccent,
                        focusedIndicatorColor = colorLimeAccent,
                        unfocusedIndicatorColor = colorTextSecondary.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Password",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
                TextField(
                    value = estado.clave,
                    onValueChange = viewModel::onClaveChange,
                    placeholder = {
                        Text(
                            text = "••••••••••••",
                            color = colorTextSecondary.copy(alpha = 0.65f),
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            IconButton(
                                onClick = { mostrarClave = !mostrarClave },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (mostrarClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Mostrar u ocultar clave",
                                    tint = colorTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "¿Olvidaste?",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable {
                                    viewModel.limpiarEstadoOtp()
                                    codigoOtp = ""
                                    mostrarDialogoGoogle = true
                                }
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = colorLimeAccent,
                        focusedIndicatorColor = colorLimeAccent,
                        unfocusedIndicatorColor = colorTextSecondary.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (estado.error != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = Color(0xFF4A1228),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF5252)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = estado.error.orEmpty(),
                        color = Color(0xFFFFCDD2),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.iniciarSesion(onLoginExitoso) },
                enabled = !estado.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorLimeAccent,
                    contentColor = Color(0xFF14071E),
                    disabledContainerColor = colorLimeAccent.copy(alpha = 0.5f),
                    disabledContentColor = Color(0xFF14071E)
                )
            ) {
                if (estado.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = Color(0xFF14071E)
                    )
                } else {
                    Text(
                        text = "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.width(36.dp),
                    color = colorTextSecondary.copy(alpha = 0.35f)
                )
                Text(
                    text = "  O recuperar acceso  ",
                    color = colorTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(
                    modifier = Modifier.width(36.dp),
                    color = colorTextSecondary.copy(alpha = 0.35f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick = {
                    viewModel.limpiarEstadoOtp()
                    codigoOtp = ""
                    mostrarDialogoGoogle = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, colorTextSecondary.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "G",
                            color = Color(0xFFEA4335),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Recuperación con Google",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = colorTextSecondary)) {
                        append("¿Probar cargos (Admin, Empadronador, Trámites)? ")
                    }
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold)) {
                        append("Ver cargos")
                    }
                },
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    dialogoAyuda = "ACTIVO"
                }
            )
        }
    }

    if (mostrarDialogoGoogle) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoGoogle = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "G",
                                color = Color(0xFFEA4335),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Text(
                        text = "Recuperación con Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Supabase Auth enviará un correo real a tu bandeja de Gmail con un código OTP / enlace de acceso:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = correoRecuperacion,
                        onValueChange = { correoRecuperacion = it },
                        label = { Text("1. Correo Registrado Único (@gmail.com)") },
                        placeholder = { Text("doltontomsistemas@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = telefonoRecuperacion,
                        onValueChange = { telefonoRecuperacion = it },
                        label = { Text("2. Teléfono Celular Único (2FA)") },
                        placeholder = { Text("+51930703759") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (estado.codigoOtpEnviado) {
                        OutlinedTextField(
                            value = codigoOtp,
                            onValueChange = { codigoOtp = it.take(8) },
                            label = { Text("Código recibido en tu Gmail (6 dígitos)") },
                            placeholder = { Text("Ej: 123456") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (estado.mensajeOtp != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = estado.mensajeOtp.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (estado.errorOtp != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = estado.errorOtp.orEmpty(),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (estado.codigoOtpEnviado && codigoOtp.length >= 6) {
                        Button(
                            onClick = {
                                viewModel.verificarCodigoCorreoGoogle(correoRecuperacion, codigoOtp) {
                                    mostrarDialogoGoogle = false
                                    onLoginExitoso()
                                }
                            },
                            enabled = !estado.enviandoOtp
                        ) {
                            Text("Verificar Código", fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { viewModel.enviarCodigoCorreoGoogle(correoRecuperacion) },
                        enabled = !estado.enviandoOtp
                    ) {
                        Text(
                            text = if (estado.enviandoOtp) "Enviando..." else if (estado.codigoOtpEnviado) "Reenviar Correo" else "Enviar a Gmail",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoGoogle = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (dialogoAyuda != null) {
        AlertDialog(
            onDismissRequest = { dialogoAyuda = null },
            title = { Text("Cargos y Operadores RENIEC (RBAC)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "El Administrador (@admin) puede crear nuevos usuarios y asignar cargos desde la pestaña Perfil. Toca un cargo para autocompletar:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    listOf(
                        Triple("admin", "Administrador General", "Control total + Crear Operadores y Asignar Cargos"),
                        Triple("empadronador", "Empadronador de Padrón", "Registrar y editar Padrón DNI (+50K) y Biometría"),
                        Triple("tramitador", "Gestor de Trámites DNI", "Registrar, aprobar y atender Trámites + Voucher QR"),
                        Triple("dolton", "Brigadista Integral", "Empadronamiento y Trámites de Campo"),
                        Triple("verificador", "Fiscalizador / Verificador", "Solo consulta de Padrón y validación biométrica")
                    ).forEach { (usr, cargo, desc) ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onUsuarioChange(usr)
                                    viewModel.onClaveChange("123456")
                                    dialogoAyuda = null
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "$cargo (@$usr • clave: 123456)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogoAyuda = null }) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
