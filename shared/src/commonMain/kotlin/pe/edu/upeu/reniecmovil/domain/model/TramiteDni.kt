package pe.edu.upeu.reniecmovil.domain.model

enum class TipoTramiteDni(val etiqueta: String, val costoSoles: Double) {
    INSCRIPCION("Inscripción por Primera Vez", 16.0),
    RENOVACION("Renovación de DNI", 30.0),
    DUPLICADO("Duplicado de DNI Electrónico", 33.0),
    RECTIFICACION("Rectificación de Datos / Estado Civil", 24.0),
    CAMBIO_DOMICILIO("Actualización de Domicilio / Ubigeo", 22.0);

    companion object {
        fun desdeTexto(valor: String): TipoTramiteDni =
            entries.firstOrNull { it.name.equals(valor, ignoreCase = true) } ?: RENOVACION
    }
}

enum class EstadoTramiteDni(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En Proceso"),
    OBSERVADO("Observado"),
    ATENDIDO("Atendido / Entregado");

    companion object {
        fun desdeTexto(valor: String): EstadoTramiteDni =
            entries.firstOrNull { it.name.equals(valor, ignoreCase = true) } ?: PENDIENTE
    }
}

data class TramiteDni(
    val id: Long = 0L,
    val dniCiudadano: String,
    val nombreCiudadano: String,
    val tipoTramite: String = TipoTramiteDni.RENOVACION.name,
    val estado: String = EstadoTramiteDni.PENDIENTE.name,
    val ubigeoDestino: String,
    val observaciones: String,
    val fechaRegistro: String = "2026-11-04",
    val pendienteSync: Boolean = false
)
