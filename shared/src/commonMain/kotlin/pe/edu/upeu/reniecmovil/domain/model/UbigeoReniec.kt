package pe.edu.upeu.reniecmovil.domain.model

data class UbigeoReniec(
    val codigo: String,
    val departamento: String,
    val provincia: String,
    val distrito: String,
    val agenciaReniec: String = "Oficina Registral RENIEC $distrito"
) {
    val etiquetaCompleta: String
        get() = "$codigo - $departamento / $provincia / $distrito"

    val etiquetaCorta: String
        get() = "$departamento • $distrito ($codigo)"
}

object CatalogoUbigeoReniec {
    val sedes: List<UbigeoReniec> = listOf(
        // 01 AMAZONAS
        UbigeoReniec("010101", "AMAZONAS", "CHACHAPOYAS", "CHACHAPOYAS", "Oficina Registral RENIEC Chachapoyas"),
        UbigeoReniec("010201", "AMAZONAS", "BAGUA", "BAGUA", "Agencia RENIEC Bagua"),
        UbigeoReniec("010701", "AMAZONAS", "UTCUBAMBA", "BAGUA GRANDE", "Agencia RENIEC Bagua Grande"),
        // 02 ANCASH
        UbigeoReniec("020101", "ANCASH", "HUARAZ", "HUARAZ", "Oficina Registral RENIEC Huaraz"),
        UbigeoReniec("020105", "ANCASH", "HUARAZ", "INDEPENDENCIA", "Punto PIDE Independencia - Huaraz"),
        UbigeoReniec("021801", "ANCASH", "SANTA", "CHIMBOTE", "Oficina Registral RENIEC Chimbote"),
        UbigeoReniec("021809", "ANCASH", "SANTA", "NUEVO CHIMBOTE", "Agencia RENIEC Nuevo Chimbote"),
        // 03 APURIMAC
        UbigeoReniec("030101", "APURIMAC", "ABANCAY", "ABANCAY", "Oficina Registral RENIEC Abancay"),
        UbigeoReniec("030201", "APURIMAC", "ANDAHUAYLAS", "ANDAHUAYLAS", "Agencia RENIEC Andahuaylas"),
        // 04 AREQUIPA
        UbigeoReniec("040101", "AREQUIPA", "AREQUIPA", "AREQUIPA", "Oficina Registral RENIEC Arequipa"),
        UbigeoReniec("040103", "AREQUIPA", "AREQUIPA", "CAYMA", "Agencia RENIEC Cayma"),
        UbigeoReniec("040104", "AREQUIPA", "AREQUIPA", "CERRO COLORADO", "Agencia RENIEC Cerro Colorado"),
        UbigeoReniec("040201", "AREQUIPA", "CAMANA", "CAMANA", "Agencia RENIEC Camaná"),
        UbigeoReniec("040701", "AREQUIPA", "ISLAY", "MOLLENDO", "Agencia RENIEC Mollendo"),
        // 05 AYACUCHO
        UbigeoReniec("050101", "AYACUCHO", "HUAMANGA", "AYACUCHO", "Oficina Registral RENIEC Ayacucho"),
        UbigeoReniec("050110", "AYACUCHO", "HUAMANGA", "CARMEN ALTO", "Punto PIDE Carmen Alto"),
        UbigeoReniec("050401", "AYACUCHO", "HUANTA", "HUANTA", "Agencia RENIEC Huanta"),
        // 06 CAJAMARCA
        UbigeoReniec("060101", "CAJAMARCA", "CAJAMARCA", "CAJAMARCA", "Oficina Registral RENIEC Cajamarca"),
        UbigeoReniec("060102", "CAJAMARCA", "CAJAMARCA", "LOS BAÑOS DEL INCA", "Punto PIDE Baños del Inca"),
        UbigeoReniec("060801", "CAJAMARCA", "JAEN", "JAEN", "Oficina Registral RENIEC Jaén"),
        // 07 CALLAO
        UbigeoReniec("070101", "CALLAO", "CALLAO", "CALLAO", "Oficina Registral RENIEC Callao"),
        UbigeoReniec("070102", "CALLAO", "CALLAO", "BELLAVISTA", "Agencia RENIEC Bellavista"),
        UbigeoReniec("070106", "CALLAO", "CALLAO", "VENTANILLA", "Agencia RENIEC Ventanilla"),
        // 08 CUSCO
        UbigeoReniec("080101", "CUSCO", "CUSCO", "CUSCO", "Oficina Registral RENIEC Cusco"),
        UbigeoReniec("080105", "CUSCO", "CUSCO", "SAN SEBASTIAN", "Agencia RENIEC San Sebastián"),
        UbigeoReniec("080108", "CUSCO", "CUSCO", "WANCHAQ", "Agencia RENIEC Wánchaq"),
        UbigeoReniec("080601", "CUSCO", "CANCHIS", "SICUANI", "Agencia RENIEC Sicuani"),
        UbigeoReniec("080901", "CUSCO", "LA CONVENCION", "SANTA ANA", "Agencia RENIEC Quillabamba"),
        // 09 HUANCAVELICA
        UbigeoReniec("090101", "HUANCAVELICA", "HUANCAVELICA", "HUANCAVELICA", "Oficina Registral RENIEC Huancavelica"),
        UbigeoReniec("090701", "HUANCAVELICA", "TAYACAJA", "PAMPAS", "Agencia RENIEC Pampas Tayacaja"),
        // 10 HUANUCO
        UbigeoReniec("100101", "HUANUCO", "HUANUCO", "HUANUCO", "Oficina Registral RENIEC Huánuco"),
        UbigeoReniec("100601", "HUANUCO", "LEONCIO PRADO", "RUPA-RUPA (TINGO MARIA)", "Agencia RENIEC Tingo María"),
        // 11 ICA
        UbigeoReniec("110101", "ICA", "ICA", "ICA", "Oficina Registral RENIEC Ica"),
        UbigeoReniec("110201", "ICA", "CHINCHA", "CHINCHA ALTA", "Agencia RENIEC Chincha Alta"),
        UbigeoReniec("110501", "ICA", "PISCO", "PISCO", "Agencia RENIEC Pisco"),
        // 12 JUNIN
        UbigeoReniec("120101", "JUNIN", "HUANCAYO", "HUANCAYO", "Oficina Registral RENIEC Huancayo"),
        UbigeoReniec("120114", "JUNIN", "HUANCAYO", "EL TAMBO", "Agencia RENIEC Huancayo - El Tambo"),
        UbigeoReniec("120301", "JUNIN", "CHANCHAMAYO", "LA MERCED", "Agencia RENIEC La Merced"),
        UbigeoReniec("120601", "JUNIN", "SATIPO", "SATIPO", "Agencia RENIEC Satipo"),
        // 13 LA LIBERTAD
        UbigeoReniec("130101", "LA LIBERTAD", "TRUJILLO", "TRUJILLO", "Oficina Registral RENIEC Trujillo"),
        UbigeoReniec("130102", "LA LIBERTAD", "TRUJILLO", "EL PORVENIR", "Agencia RENIEC El Porvenir"),
        UbigeoReniec("130105", "LA LIBERTAD", "TRUJILLO", "LA ESPERANZA", "Agencia RENIEC La Esperanza"),
        UbigeoReniec("130701", "LA LIBERTAD", "PACASMAYO", "SAN PEDRO DE LLOC", "Agencia RENIEC Pacasmayo"),
        // 14 LAMBAYEQUE
        UbigeoReniec("140101", "LAMBAYEQUE", "CHICLAYO", "CHICLAYO", "Oficina Registral RENIEC Chiclayo"),
        UbigeoReniec("140105", "LAMBAYEQUE", "CHICLAYO", "JOSE LEONARDO ORTIZ", "Agencia RENIEC José Leonardo Ortiz"),
        UbigeoReniec("140301", "LAMBAYEQUE", "LAMBAYEQUE", "LAMBAYEQUE", "Agencia RENIEC Lambayeque"),
        // 15 LIMA
        UbigeoReniec("150118", "LIMA", "LIMA", "LURIGANCHO-CHOSICA", "Agencia RENIEC Ñaña - Carretera Central"),
        UbigeoReniec("150103", "LIMA", "LIMA", "ATE", "Agencia RENIEC Santa Clara - Ate"),
        UbigeoReniec("150101", "LIMA", "LIMA", "CERCADO DE LIMA", "Sede Central RENIEC Jr. Bolivia"),
        UbigeoReniec("150132", "LIMA", "LIMA", "SAN JUAN DE LURIGANCHO", "Agencia RENIEC San Juan de Lurigancho"),
        UbigeoReniec("150122", "LIMA", "LIMA", "MIRAFLORES", "Agencia RENIEC Miraflores"),
        UbigeoReniec("150110", "LIMA", "LIMA", "COMAS", "Agencia RENIEC Comas"),
        UbigeoReniec("150501", "LIMA", "CAÑETE", "SAN VICENTE DE CAÑETE", "Agencia RENIEC Cañete"),
        UbigeoReniec("150801", "LIMA", "HUAURA", "HUACHO", "Oficina Registral RENIEC Huacho"),
        // 16 LORETO
        UbigeoReniec("160101", "LORETO", "MAYNAS", "IQUITOS", "Brigada Fluvial RENIEC Iquitos"),
        UbigeoReniec("160108", "LORETO", "MAYNAS", "PUNCHANA", "Agencia RENIEC Punchana"),
        UbigeoReniec("160201", "LORETO", "ALTO AMAZONAS", "YURIMAGUAS", "Agencia RENIEC Yurimaguas"),
        // 17 MADRE DE DIOS
        UbigeoReniec("170101", "MADRE DE DIOS", "TAMBOPATA", "TAMBOPATA", "Oficina Registral RENIEC Puerto Maldonado"),
        // 18 MOQUEGUA
        UbigeoReniec("180101", "MOQUEGUA", "MARISCAL NIETO", "MOQUEGUA", "Oficina Registral RENIEC Moquegua"),
        UbigeoReniec("180301", "MOQUEGUA", "ILO", "ILO", "Agencia RENIEC Ilo"),
        // 19 PASCO
        UbigeoReniec("190101", "PASCO", "PASCO", "CHAUPIMARCA", "Oficina Registral RENIEC Cerro de Pasco"),
        UbigeoReniec("190301", "PASCO", "OXAPAMPA", "OXAPAMPA", "Agencia RENIEC Oxapampa"),
        // 20 PIURA
        UbigeoReniec("200101", "PIURA", "PIURA", "PIURA", "Oficina Registral RENIEC Piura"),
        UbigeoReniec("200104", "PIURA", "PIURA", "CASTILLA", "Agencia RENIEC Castilla"),
        UbigeoReniec("200601", "PIURA", "SULLANA", "SULLANA", "Agencia RENIEC Sullana"),
        UbigeoReniec("200701", "PIURA", "TALARA", "PARIÑAS", "Agencia RENIEC Talara"),
        // 21 PUNO
        UbigeoReniec("211101", "PUNO", "SAN ROMAN", "JULIACA", "Agencia Regional RENIEC Juliaca"),
        UbigeoReniec("211105", "PUNO", "SAN ROMAN", "SAN MIGUEL", "Punto PIDE San Miguel - Juliaca"),
        UbigeoReniec("211102", "PUNO", "SAN ROMAN", "CABANILLAS", "Punto PIDE Cabanillas"),
        UbigeoReniec("210101", "PUNO", "PUNO", "PUNO", "Oficina Registral RENIEC Puno"),
        UbigeoReniec("210201", "PUNO", "AZANGARO", "AZANGARO", "Agencia RENIEC Azángaro"),
        UbigeoReniec("210501", "PUNO", "EL COLLAO", "ILAVE", "Agencia RENIEC Ilave"),
        // 22 SAN MARTIN
        UbigeoReniec("220901", "SAN MARTIN", "SAN MARTIN", "TARAPOTO", "Oficina Registral RENIEC Tarapoto"),
        UbigeoReniec("220101", "SAN MARTIN", "MOYOBAMBA", "MOYOBAMBA", "Oficina Registral RENIEC Moyobamba"),
        UbigeoReniec("220801", "SAN MARTIN", "RIOJA", "RIOJA", "Agencia RENIEC Rioja"),
        // 23 TACNA
        UbigeoReniec("230101", "TACNA", "TACNA", "TACNA", "Oficina Registral RENIEC Tacna"),
        UbigeoReniec("230102", "TACNA", "TACNA", "ALTO DE LA ALIANZA", "Agencia RENIEC Alto de la Alianza"),
        UbigeoReniec("230110", "TACNA", "TACNA", "CORONEL GREGORIO ALBARRACIN", "Agencia RENIEC Gregorio Albarracín"),
        // 24 TUMBES
        UbigeoReniec("240101", "TUMBES", "TUMBES", "TUMBES", "Oficina Registral RENIEC Tumbes"),
        UbigeoReniec("240301", "TUMBES", "ZARUMILLA", "ZARUMILLA", "CEBAF Frontera Zarumilla"),
        // 25 UCAYALI
        UbigeoReniec("250101", "UCAYALI", "CORONEL PORTILLO", "CALLERIA (PUCALLPA)", "Oficina Registral RENIEC Pucallpa"),
        UbigeoReniec("250105", "UCAYALI", "CORONEL PORTILLO", "YARINACOCHA", "Agencia RENIEC Yarinacocha"),
        UbigeoReniec("250301", "UCAYALI", "PADRE ABAD", "PADRE ABAD (AGUAYTIA)", "Agencia RENIEC Aguaytía")
    )

    // Ordenamos poniendo primero los departamentos más consultados y luego el resto de las 25 regiones
    val departamentos: List<String> = listOf("TODOS") + listOf(
        "LIMA", "PUNO", "AREQUIPA", "CUSCO", "LA LIBERTAD", "PIURA", "JUNIN",
        "CAJAMARCA", "LAMBAYEQUE", "ANCASH", "SAN MARTIN", "LORETO", "ICA",
        "HUANUCO", "AYACUCHO", "UCAYALI", "APURIMAC", "AMAZONAS", "TACNA",
        "PASCO", "TUMBES", "MOQUEGUA", "HUANCAVELICA", "MADRE DE DIOS", "CALLAO"
    )

    val departamentosSinTodos: List<String> = departamentos.filter { it != "TODOS" }

    fun provinciasPorDepartamento(departamento: String): List<String> =
        if (departamento == "TODOS") {
            sedes.map { it.provincia }.distinct()
        } else {
            sedes.filter { it.departamento.equals(departamento, ignoreCase = true) }
                .map { it.provincia }
                .distinct()
        }

    fun distritosPorProvincia(departamento: String, provincia: String): List<UbigeoReniec> =
        sedes.filter {
            (departamento == "TODOS" || it.departamento.equals(departamento, ignoreCase = true)) &&
                (provincia == "TODAS" || provincia.isBlank() || it.provincia.equals(provincia, ignoreCase = true))
        }

    fun detectarDepartamento(textoUbigeo: String): String {
        val upper = textoUbigeo.uppercase()
        return sedes.firstOrNull {
            upper.contains(it.codigo) || upper.contains("${it.departamento} /") || upper.contains(it.departamento)
        }?.departamento ?: "LIMA"
    }

    fun detectarProvincia(textoUbigeo: String): String {
        val upper = textoUbigeo.uppercase()
        return sedes.firstOrNull {
            upper.contains(it.codigo) || upper.contains("/ ${it.provincia} /") || upper.contains(it.provincia)
        }?.provincia ?: ""
    }

    fun detectarDistrito(textoUbigeo: String): String {
        val upper = textoUbigeo.uppercase()
        return sedes.firstOrNull {
            upper.contains(it.codigo) || upper.contains(it.distrito)
        }?.distrito ?: ""
    }

    fun buscarUbigeo(textoUbigeo: String): UbigeoReniec? {
        val upper = textoUbigeo.uppercase()
        return sedes.firstOrNull {
            upper.contains(it.codigo) || upper.contains(it.distrito)
        } ?: sedes.firstOrNull { upper.contains(it.departamento) }
    }
}
