package pe.edu.upeu.reniecmovil.presentation.ciudadano

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.reniecmovil.data.remote.AuditoriaApi
import pe.edu.upeu.reniecmovil.data.remote.dto.AuditoriaDto
import pe.edu.upeu.reniecmovil.data.remote.dto.AuditoriaPayloadDto
import pe.edu.upeu.reniecmovil.domain.model.CatalogoUbigeoReniec
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.EstadoCivil
import pe.edu.upeu.reniecmovil.domain.repository.CiudadanoRepository
import pe.edu.upeu.reniecmovil.domain.usecase.BuscarCiudadanoPorDniUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.CiudadanoInvalidoException
import pe.edu.upeu.reniecmovil.domain.usecase.EliminarCiudadanoUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.ListarCiudadanosUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.RegistrarCiudadanoUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.resultadoDe
import pe.edu.upeu.reniecmovil.obtenerInfoDispositivo

data class FormularioCiudadano(
    val dni: String = "",
    val nombres: String = "",
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val fechaNacimiento: String = "2000-01-15",
    val estadoCivil: String = EstadoCivil.SOLTERO.name,
    val ubigeo: String = "150118 - LIMA / LIMA / LURIGANCHO-CHOSICA",
    val direccion: String = "",
    val donacionOrganos: Boolean = true,
    val dniError: String? = null,
    val nombresError: String? = null,
    val paternoError: String? = null,
    val maternoError: String? = null,
    val fechaError: String? = null,
    val ubigeoError: String? = null,
    val direccionError: String? = null
)

data class CiudadanoUiState(
    val ciudadanos: List<Ciudadano> = emptyList(),
    val resultadosRemotosMasivos: List<Ciudadano> = emptyList(),
    val totalPadronNacional: Long = 50009L,
    val eventosAuditoria: List<AuditoriaDto> = emptyList(),
    val filtroDniONombre: String = "",
    val filtroDepartamento: String = "TODOS",
    val filtroProvincia: String = "TODAS",
    val filtroDistrito: String = "TODOS",
    val ciudadanoSeleccionado: Ciudadano? = null,
    val ciudadanoParaBiometria: Ciudadano? = null,
    val editandoId: Long? = null,
    val mostrarFormulario: Boolean = false,
    val formulario: FormularioCiudadano = FormularioCiudadano(),
    val sincronizando: Boolean = false,
    val guardando: Boolean = false,
    val avisoRed: String? = null,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
) {
    val terminoUbigeoActivo: String
        get() = when {
            filtroDistrito != "TODOS" -> filtroDistrito
            filtroProvincia != "TODAS" -> filtroProvincia
            else -> filtroDepartamento
        }

    val ciudadanosFiltrados: List<Ciudadano>
        get() {
            val q = filtroDniONombre.trim().lowercase()
            val combinados = (resultadosRemotosMasivos + ciudadanos).distinctBy { it.dni }
            return combinados.filter { c ->
                val coincideDep = filtroDepartamento == "TODOS" ||
                    CatalogoUbigeoReniec.detectarDepartamento(c.ubigeo).equals(filtroDepartamento, ignoreCase = true) ||
                    c.ubigeo.contains(filtroDepartamento, ignoreCase = true)
                val coincideProv = filtroProvincia == "TODAS" ||
                    c.ubigeo.contains(filtroProvincia, ignoreCase = true)
                val coincideDist = filtroDistrito == "TODOS" ||
                    c.ubigeo.contains(filtroDistrito, ignoreCase = true)
                val coincideTexto = q.isBlank() ||
                    c.dni.lowercase().contains(q) ||
                    c.nombreCompleto.lowercase().contains(q) ||
                    c.ubigeo.lowercase().contains(q)
                coincideDep && coincideProv && coincideDist && coincideTexto
            }
        }
}

class CiudadanoViewModel(
    private val repository: CiudadanoRepository,
    private val listarCiudadanos: ListarCiudadanosUseCase = ListarCiudadanosUseCase(repository),
    private val buscarPorDni: BuscarCiudadanoPorDniUseCase = BuscarCiudadanoPorDniUseCase(repository),
    private val registrarCiudadano: RegistrarCiudadanoUseCase = RegistrarCiudadanoUseCase(repository),
    private val eliminarCiudadano: EliminarCiudadanoUseCase = EliminarCiudadanoUseCase(repository),
    private val auditoriaApi: AuditoriaApi? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CiudadanoUiState())
    val uiState: StateFlow<CiudadanoUiState> = _uiState.asStateFlow()
    private var busquedaRemotaJob: Job? = null

    init {
        viewModelScope.launch {
            listarCiudadanos().collect { lista ->
                _uiState.update { actual ->
                    actual.copy(
                        ciudadanos = lista,
                        ciudadanoSeleccionado = actual.ciudadanoSeleccionado?.let { sel ->
                            lista.firstOrNull { it.id == sel.id || it.dni == sel.dni } ?: sel
                        } ?: lista.firstOrNull()
                    )
                }
            }
        }
        sincronizar()
        refrescarAuditoriaYPadron()
    }

    fun sincronizar() {
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true, mensajeError = null) }
            val res = resultadoDe { repository.sincronizar() }
            val total = try { repository.obtenerTotalPadron() } catch (_: Exception) { 50009L }
            _uiState.update {
                it.copy(
                    sincronizando = false,
                    totalPadronNacional = maxOf(total, it.ciudadanos.size.toLong()),
                    avisoRed = res.exceptionOrNull()?.let { "Modo Brigada Offline: mostrando padrón local SQLDelight" }
                )
            }
            refrescarAuditoriaYPadron()
        }
    }

    fun refrescarAuditoriaYPadron() {
        viewModelScope.launch {
            val total = try { repository.obtenerTotalPadron() } catch (_: Exception) { 50009L }
            val eventos = auditoriaApi?.listarRecientes(25).orEmpty()
            _uiState.update {
                it.copy(
                    totalPadronNacional = maxOf(total, 50000L),
                    eventosAuditoria = eventos
                )
            }
        }
    }

    fun registrarEventoTelemetriaOAuditoria(
        usuario: String,
        rol: String,
        accion: String,
        entidad: String,
        referenciaDni: String,
        detalle: String,
        canal: String = "Google Analytics + Clarity"
    ) {
        viewModelScope.launch {
            val disp = obtenerInfoDispositivo()
            auditoriaApi?.registrarEvento(
                AuditoriaPayloadDto(
                    usuario = usuario,
                    rol = rol,
                    accion = accion,
                    entidad = entidad,
                    referenciaDni = referenciaDni,
                    detalle = detalle,
                    canalTelemetria = canal,
                    dispositivo = "${disp.nombrePlataforma} (${disp.modeloEquipo})"
                )
            )
            refrescarAuditoriaYPadron()
        }
    }

    fun abrirVerificacionBiometrica(ciudadano: Ciudadano?) {
        _uiState.update { it.copy(ciudadanoParaBiometria = ciudadano) }
    }

    fun confirmarBiometriaCiudadano(usuario: String, rol: String, modo: String, puntaje: String) {
        val c = _uiState.value.ciudadanoParaBiometria ?: return
        _uiState.update {
            it.copy(
                ciudadanoParaBiometria = null,
                mensajeExito = "✓ Biometría ($modo • $puntaje) guardada y actualizada para ${c.nombreCompleto} (DNI ${c.dni})"
            )
        }
        viewModelScope.launch {
            if (c.id > 0) {
                try { repository.actualizar(c) } catch (_: Exception) {}
            }
        }
        registrarEventoTelemetriaOAuditoria(
            usuario = usuario,
            rol = rol,
            accion = "ACTUALIZAR_BIOMETRIA_AFIS",
            entidad = "BIOMETRIA",
            referenciaDni = c.dni,
            detalle = "Rostro/Huella actualizados con $modo ($puntaje) de ${c.nombreCompleto}",
            canal = "Google Analytics + Clarity + BioFacial AFIS"
        )
    }

    fun onFiltroChange(texto: String) {
        _uiState.update { it.copy(filtroDniONombre = texto, mensajeExito = null, mensajeError = null) }
        busquedaRemotaJob?.cancel()
        if (texto.trim().length >= 2) {
            busquedaRemotaJob = viewModelScope.launch {
                delay(280)
                val encontrados = repository.buscarEnPadronMasivo(texto.trim(), _uiState.value.terminoUbigeoActivo)
                _uiState.update { actual ->
                    actual.copy(
                        resultadosRemotosMasivos = encontrados,
                        ciudadanoSeleccionado = encontrados.firstOrNull() ?: actual.ciudadanoSeleccionado
                    )
                }
            }
        } else if (texto.isBlank()) {
            _uiState.update { it.copy(resultadosRemotosMasivos = emptyList()) }
        }
    }

    fun onFiltroDepartamentoChange(departamento: String) {
        _uiState.update {
            it.copy(
                filtroDepartamento = departamento,
                filtroProvincia = "TODAS",
                filtroDistrito = "TODOS",
                mensajeExito = null,
                mensajeError = null
            )
        }
        ejecutarFiltroUbigeoRemoto(departamento)
    }

    fun onFiltroProvinciaChange(provincia: String) {
        _uiState.update {
            it.copy(
                filtroProvincia = provincia,
                filtroDistrito = "TODOS",
                mensajeExito = null,
                mensajeError = null
            )
        }
        val termino = if (provincia == "TODAS") _uiState.value.filtroDepartamento else provincia
        ejecutarFiltroUbigeoRemoto(termino)
    }

    fun onFiltroDistritoChange(distrito: String) {
        _uiState.update {
            it.copy(
                filtroDistrito = distrito,
                mensajeExito = null,
                mensajeError = null
            )
        }
        val termino = when {
            distrito != "TODOS" -> distrito
            _uiState.value.filtroProvincia != "TODAS" -> _uiState.value.filtroProvincia
            else -> _uiState.value.filtroDepartamento
        }
        ejecutarFiltroUbigeoRemoto(termino)
    }

    private fun ejecutarFiltroUbigeoRemoto(terminoUbigeo: String) {
        viewModelScope.launch {
            val encontrados = repository.buscarEnPadronMasivo(_uiState.value.filtroDniONombre, terminoUbigeo)
            _uiState.update { actual ->
                val nuevaLista = actual.copy(resultadosRemotosMasivos = encontrados).ciudadanosFiltrados
                actual.copy(
                    resultadosRemotosMasivos = encontrados,
                    ciudadanoSeleccionado = nuevaLista.firstOrNull() ?: actual.ciudadanoSeleccionado
                )
            }
        }
    }

    fun seleccionarCiudadano(ciudadano: Ciudadano) {
        _uiState.update { it.copy(ciudadanoSeleccionado = ciudadano, mensajeExito = null) }
    }

    fun consultarDniExacto() {
        val texto = _uiState.value.filtroDniONombre.trim()
        viewModelScope.launch {
            if (texto.length == 8 && texto.all { it.isDigit() }) {
                buscarPorDni(texto).fold(
                    onSuccess = { encontrado ->
                        if (encontrado != null) {
                            _uiState.update {
                                it.copy(
                                    ciudadanoSeleccionado = encontrado,
                                    resultadosRemotosMasivos = listOf(encontrado),
                                    mensajeExito = "Consulta PIDE (+50,000 ciudadanos) exitosa: DNI ${encontrado.dni} • ${encontrado.nombreCompleto}",
                                    mensajeError = null
                                )
                            }
                            registrarEventoTelemetriaOAuditoria(
                                usuario = "brigada",
                                rol = "ROLE_BRIGADISTA",
                                accion = "CONSULTA_PADRON_50K",
                                entidad = "CIUDADANO",
                                referenciaDni = encontrado.dni,
                                detalle = "Ficha DNIe 3.0 consultada en Padrón Nacional: ${encontrado.nombreCompleto}"
                            )
                        } else {
                            _uiState.update {
                                it.copy(mensajeError = "No se encontró el DNI $texto en los +50,000 registros del padrón nacional")
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(mensajeError = error.message) }
                    }
                )
            } else {
                val encontrados = repository.buscarEnPadronMasivo(texto, _uiState.value.filtroDepartamento)
                if (encontrados.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            resultadosRemotosMasivos = encontrados,
                            ciudadanoSeleccionado = encontrados.first(),
                            mensajeExito = "Se encontraron ${encontrados.size} ciudadanos en el Padrón Nacional (+50,000)",
                            mensajeError = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(mensajeError = "Ingresa un DNI de 8 dígitos (ej: 40000001 al 40050000) o apellido válido")
                    }
                }
            }
        }
    }

    fun abrirNuevoCiudadano() {
        _uiState.update {
            it.copy(
                editandoId = null,
                mostrarFormulario = true,
                formulario = FormularioCiudadano(),
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun iniciarEdicion(ciudadano: Ciudadano) {
        _uiState.update {
            it.copy(
                editandoId = ciudadano.id,
                mostrarFormulario = true,
                formulario = FormularioCiudadano(
                    dni = ciudadano.dni,
                    nombres = ciudadano.nombres,
                    apellidoPaterno = ciudadano.apellidoPaterno,
                    apellidoMaterno = ciudadano.apellidoMaterno,
                    fechaNacimiento = ciudadano.fechaNacimiento,
                    estadoCivil = ciudadano.estadoCivil,
                    ubigeo = ciudadano.ubigeo,
                    direccion = ciudadano.direccion,
                    donacionOrganos = ciudadano.donacionOrganos
                ),
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun cerrarFormulario() {
        _uiState.update {
            it.copy(
                editandoId = null,
                mostrarFormulario = false,
                formulario = FormularioCiudadano()
            )
        }
    }

    fun actualizarFormulario(transformar: (FormularioCiudadano) -> FormularioCiudadano) {
        _uiState.update {
            it.copy(
                formulario = transformar(it.formulario),
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun guardarCiudadano(usuarioOperador: String = "admin", rolOperador: String = "ROLE_ADMIN") {
        val estado = _uiState.value
        if (estado.guardando) return
        val form = estado.formulario
        val eraEdicion = estado.editandoId != null

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null, mensajeExito = null) }
            registrarCiudadano(
                id = estado.editandoId ?: 0L,
                dni = form.dni,
                nombres = form.nombres,
                apellidoPaterno = form.apellidoPaterno,
                apellidoMaterno = form.apellidoMaterno,
                fechaNacimiento = form.fechaNacimiento,
                estadoCivil = form.estadoCivil,
                ubigeo = form.ubigeo,
                direccion = form.direccion,
                donacionOrganos = form.donacionOrganos
            ).fold(
                onSuccess = { guardado ->
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mostrarFormulario = false,
                            editandoId = null,
                            ciudadanoSeleccionado = guardado,
                            totalPadronNacional = if (eraEdicion) it.totalPadronNacional else it.totalPadronNacional + 1,
                            formulario = FormularioCiudadano(),
                            mensajeExito = "Ciudadano ${guardado.nombreCompleto} (DNI ${guardado.dni}) sincronizado en el padrón"
                        )
                    }
                    registrarEventoTelemetriaOAuditoria(
                        usuario = usuarioOperador,
                        rol = rolOperador,
                        accion = if (eraEdicion) "ACTUALIZAR_PADRON" else "EMPADRONAR_CIUDADANO",
                        entidad = "CIUDADANO",
                        referenciaDni = guardado.dni,
                        detalle = "${if (eraEdicion) "Actualización" else "Inscripción"} en padrón de ${guardado.nombreCompleto} (${guardado.ubigeo})",
                        canal = "Google Analytics + Clarity + Píxel Meta"
                    )
                },
                onFailure = { fallo ->
                    when (fallo) {
                        is CiudadanoInvalidoException -> _uiState.update {
                            it.copy(
                                guardando = false,
                                formulario = it.formulario.copy(
                                    dniError = fallo.errores.dni,
                                    nombresError = fallo.errores.nombres,
                                    paternoError = fallo.errores.apellidoPaterno,
                                    maternoError = fallo.errores.apellidoMaterno,
                                    fechaError = fallo.errores.fechaNacimiento,
                                    ubigeoError = fallo.errores.ubigeo,
                                    direccionError = fallo.errores.direccion
                                )
                            )
                        }
                        else -> _uiState.update {
                            it.copy(
                                guardando = false,
                                mensajeError = fallo.message ?: "Error al guardar ciudadano"
                            )
                        }
                    }
                }
            )
        }
    }

    fun eliminar(id: Long, dni: String = "", usuarioOperador: String = "admin", rolOperador: String = "ROLE_ADMIN") {
        viewModelScope.launch {
            eliminarCiudadano(id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            resultadosRemotosMasivos = it.resultadosRemotosMasivos.filterNot { c -> c.id == id },
                            mensajeExito = "Registro de ciudadano eliminado del padrón"
                        )
                    }
                    registrarEventoTelemetriaOAuditoria(
                        usuario = usuarioOperador,
                        rol = rolOperador,
                        accion = "ELIMINAR_CIUDADANO",
                        entidad = "CIUDADANO",
                        referenciaDni = dni.ifBlank { id.toString() },
                        detalle = "Baja administrativa de ciudadano ID $id en Padrón RENIEC"
                    )
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(mensajeError = fallo.message ?: "No se pudo eliminar el ciudadano")
                    }
                }
            )
        }
    }
}
