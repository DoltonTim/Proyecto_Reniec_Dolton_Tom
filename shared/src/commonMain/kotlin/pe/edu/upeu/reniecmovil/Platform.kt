package pe.edu.upeu.reniecmovil

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

data class InfoDispositivo(
    val nombrePlataforma: String,
    val modeloEquipo: String,
    val modoOfflineSoportado: Boolean = true
)

expect fun obtenerInfoDispositivo(): InfoDispositivo

object BiometriaSesionCache {
    val fotosPorDni = mutableMapOf<String, ImageBitmap>()
    val bytesFotoPorDni = mutableMapOf<String, ByteArray>()
    val fotosNoEncontradasEnNube = mutableSetOf<String>()
    val estadoFacialPorDni = mutableMapOf<String, String>()
    val estadoHuellaPorDni = mutableMapOf<String, String>()
}

expect fun imageBitmapAJpegBytes(imagen: ImageBitmap): ByteArray?

expect fun jpegBytesAImageBitmap(bytes: ByteArray): ImageBitmap?

@Composable
expect fun BotonesCapturaHardwareNativo(
    dni: String,
    onResultadoRostro: (detalle: String, foto: ImageBitmap?) -> Unit,
    onResultadoHuella: (detalle: String) -> Unit
)
