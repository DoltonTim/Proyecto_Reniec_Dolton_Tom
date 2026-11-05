package pe.edu.upeu.reniecmovil

import platform.UIKit.UIDevice

actual fun obtenerInfoDispositivo(): InfoDispositivo = InfoDispositivo(
    nombrePlataforma = "${UIDevice.currentDevice.systemName()} ${UIDevice.currentDevice.systemVersion}",
    modeloEquipo = UIDevice.currentDevice.model
)
