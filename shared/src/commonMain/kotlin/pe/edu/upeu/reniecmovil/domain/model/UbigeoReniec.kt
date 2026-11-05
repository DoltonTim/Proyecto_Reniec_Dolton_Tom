package pe.edu.upeu.reniecmovil.domain.model

data class UbigeoReniec(
    val codigo: String,
    val departamento: String,
    val provincia: String,
    val distrito: String,
    val agenciaReniec: String
) {
    val etiquetaCompleta: String
        get() = "$codigo - $departamento / $provincia / $distrito"

    val etiquetaCorta: String
        get() = "$departamento • $distrito ($codigo)"
}

object CatalogoUbigeoReniec {
    val sedes: List<UbigeoReniec> = listOf(
        UbigeoReniec("150118", "LIMA", "LIMA", "LURIGANCHO-CHOSICA", "Agencia RENIEC Ñaña - Carretera Central"),
        UbigeoReniec("150103", "LIMA", "LIMA", "ATE", "Agencia RENIEC Santa Clara - Ate"),
        UbigeoReniec("150101", "LIMA", "LIMA", "CERCADO DE LIMA", "Sede Central RENIEC Jr. Bolivia"),
        UbigeoReniec("211101", "PUNO", "SAN ROMAN", "JULIACA", "Agencia Regional RENIEC Juliaca"),
        UbigeoReniec("210101", "PUNO", "PUNO", "PUNO", "Oficina Registral RENIEC Puno"),
        UbigeoReniec("120114", "JUNIN", "HUANCAYO", "EL TAMBO", "Agencia RENIEC Huancayo - El Tambo"),
        UbigeoReniec("220901", "SAN MARTIN", "SAN MARTIN", "TARAPOTO", "Oficina Registral RENIEC Tarapoto"),
        UbigeoReniec("080101", "CUSCO", "CUSCO", "CUSCO", "Oficina Registral RENIEC Cusco"),
        UbigeoReniec("040101", "AREQUIPA", "AREQUIPA", "AREQUIPA", "Oficina Registral RENIEC Arequipa"),
        UbigeoReniec("160101", "LORETO", "MAYNAS", "IQUITOS", "Brigada Fluvial RENIEC Iquitos")
    )

    val departamentos: List<String> = listOf("TODOS") + sedes.map { it.departamento }.distinct()

    fun provinciasPorDepartamento(departamento: String): List<String> =
        sedes.filter { it.departamento.equals(departamento, ignoreCase = true) }
            .map { it.provincia }
            .distinct()

    fun distritosPorProvincia(departamento: String, provincia: String): List<UbigeoReniec> =
        sedes.filter {
            it.departamento.equals(departamento, ignoreCase = true) &&
                it.provincia.equals(provincia, ignoreCase = true)
        }

    fun detectarDepartamento(textoUbigeo: String): String {
        val upper = textoUbigeo.uppercase()
        return sedes.firstOrNull {
            upper.contains(it.departamento) || upper.contains(it.codigo) || upper.contains(it.distrito)
        }?.departamento ?: "OTROS"
    }

    fun buscarUbigeo(textoUbigeo: String): UbigeoReniec? {
        val upper = textoUbigeo.uppercase()
        return sedes.firstOrNull {
            upper.contains(it.codigo) || upper.contains(it.distrito)
        } ?: sedes.firstOrNull { upper.contains(it.departamento) }
    }
}
