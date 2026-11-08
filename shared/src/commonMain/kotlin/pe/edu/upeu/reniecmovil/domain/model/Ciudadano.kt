package pe.edu.upeu.reniecmovil.domain.model

enum class EstadoCivil(val etiqueta: String) {
    SOLTERO("Soltero(a)"),
    CASADO("Casado(a)"),
    VIUDO("Viudo(a)"),
    DIVORCIADO("Divorciado(a)");

    companion object {
        fun desdeTexto(valor: String): EstadoCivil =
            entries.firstOrNull { it.name.equals(valor, ignoreCase = true) || it.etiqueta.equals(valor, ignoreCase = true) }
                ?: SOLTERO
    }
}

data class Ciudadano(
    val id: Long = 0L,
    val dni: String,
    val nombres: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val fechaNacimiento: String,
    val estadoCivil: String = EstadoCivil.SOLTERO.name,
    val ubigeo: String,
    val direccion: String,
    val donacionOrganos: Boolean = true,
    val pendienteSync: Boolean = false
) {
    val nombreCompleto: String
        get() = "$nombres $apellidoPaterno $apellidoMaterno".trim()
}
