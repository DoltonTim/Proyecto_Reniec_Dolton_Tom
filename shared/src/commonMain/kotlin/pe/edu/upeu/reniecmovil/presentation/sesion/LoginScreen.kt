package pe.edu.upeu.reniecmovil.presentation.sesion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Nfc
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
    var dialogoAyuda by remember { mutableStateOf<String?>(null) }

    val colorHeaderTop = Color(0xFF8C0B42)      // Guinda-púrpura vibrante superior
    val colorHeaderBottom = Color(0xFF640D5F)   // Púrpura institucional intenso
    val colorDarkSurface = Color(0xFF1A0926)    // Superficie nocturna profunda (tipo The Stori)
    val colorLimeAccent = Color(0xFFC6FF00)     // Botón neón lima de alto contraste
    val colorTextSecondary = Color(0xFFBBA8C9)  // Lavanda grisáceo nítido para etiquetas

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorHeaderTop, colorHeaderBottom, colorDarkSurface)
                )
            )
    ) {
        // Fondo geométrico diagonal con pico redondeado (estilo The Stori)
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

            // Cabecera Superior con emblema de rombo y título al costado
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

            Spacer(modifier = Modifier.height(105.dp))

            // Título central "Login" dentro del pico oscuro
            Text(
                text = "Login",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(34.dp))

            // Campo 1: Usuario Institucional (con línea inferior limpia)
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
                            text = "ej: usuario@reniec.gob.pe",
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

            // Campo 2: Password + enlace "Forgot?"
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
                                    dialogoAyuda = "Para restablecer tu clave institucional JWT, comunícate con la Oficina de Seguridad Informática de RENIEC desde la intranet."
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

            // Botón principal lima brillante ("Login")
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

            Spacer(modifier = Modifier.height(28.dp))

            // Separador "— Or continue with —"
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
                    text = "  O ingresar con  ",
                    color = colorTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(
                    modifier = Modifier.width(36.dp),
                    color = colorTextSecondary.copy(alpha = 0.35f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dos botones inferiores con borde sutil (estilo Google / Facebook de la foto)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        dialogoAyuda = "Acerca tu DNI Electrónico 3.0 al lector NFC posterior del dispositivo móvil para lectura criptográfica."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colorTextSecondary.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "DNIe NFC",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DNIe NFC",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        dialogoAyuda = "Conecta el lector óptico biométrico o utiliza el sensor de huella del equipo para validación AFIS."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colorTextSecondary.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Huella AFIS",
                        tint = Color(0xFF40C4FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Huella AFIS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Texto inferior estilo "Don't have account? Register now"
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = colorTextSecondary)) {
                        append("¿No tienes cuenta activa? ")
                    }
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold)) {
                        append("Solicitar acceso")
                    }
                },
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    dialogoAyuda = "El alta de nuevos operadores y brigadistas es gestionada exclusivamente por la Jefatura Regional de Registro Civil."
                }
            )
        }
    }

    if (dialogoAyuda != null) {
        AlertDialog(
            onDismissRequest = { dialogoAyuda = null },
            title = { Text("Seguridad Institucional RENIEC", fontWeight = FontWeight.Bold) },
            text = { Text(dialogoAyuda.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { dialogoAyuda = null }) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
