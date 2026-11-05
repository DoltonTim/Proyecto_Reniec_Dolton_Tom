package pe.edu.upeu.reniecmovil

import android.os.Build

actual fun obtenerInfoDispositivo(): InfoDispositivo = InfoDispositivo(
    nombrePlataforma = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
    modeloEquipo = "${Build.MANUFACTURER} ${Build.MODEL}"
)
