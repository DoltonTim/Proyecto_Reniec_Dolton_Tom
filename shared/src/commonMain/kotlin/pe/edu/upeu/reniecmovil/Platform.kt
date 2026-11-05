package pe.edu.upeu.reniecmovil

data class InfoDispositivo(
    val nombrePlataforma: String,
    val modeloEquipo: String,
    val modoOfflineSoportado: Boolean = true
)

expect fun obtenerInfoDispositivo(): InfoDispositivo
