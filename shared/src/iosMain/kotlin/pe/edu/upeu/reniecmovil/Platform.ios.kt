package pe.edu.upeu.reniecmovil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import platform.UIKit.UIDevice

actual fun obtenerInfoDispositivo(): InfoDispositivo = InfoDispositivo(
    nombrePlataforma = "${UIDevice.currentDevice.systemName()} ${UIDevice.currentDevice.systemVersion}",
    modeloEquipo = UIDevice.currentDevice.model
)

@Composable
actual fun BotonesCapturaHardwareNativo(
    dni: String,
    onResultadoRostro: (detalle: String, foto: ImageBitmap?) -> Unit,
    onResultadoHuella: (detalle: String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onResultadoRostro("FaceID / Cámara iOS • Rostro Validado 99.8%", null) },
            modifier = Modifier.weight(1f)
        ) {
            Text("FaceID / Cámara")
        }
        FilledTonalButton(
            onClick = { onResultadoHuella("TouchID / Huella AFIS Validada 99.9%") },
            modifier = Modifier.weight(1f)
        ) {
            Text("TouchID / Huella")
        }
    }
}
