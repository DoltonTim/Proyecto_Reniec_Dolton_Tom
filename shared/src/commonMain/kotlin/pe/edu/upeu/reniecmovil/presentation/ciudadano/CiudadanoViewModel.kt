package pe.edu.upeu.reniecmovil.presentation.ciudadano

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val huellaCapturada: Boolean = true,
    val dedoSeleccionado: String = "Índice Derecho",
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
    val filtroDniONombre: String = "",
    val filtroDepartamento: String = "TODOS",
    val ciudadanoSeleccionado: Ciudadano? = null,
    val editandoId: Long? = null,
    val mostrarFormulario: Boolean = false,
    val formulario: FormularioCiudadano = FormularioCiudadano(),
    val sincronizando: Boolean = false,
    val guardando: Boolean = false,
    val avisoRed: String? = null,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
) {
    val ciudadanosFiltrados: List<Ciudadano>
        get() {
            val q = filtroDniONombre.trim().lowercase()
            return ciudadanos.filter { c ->
                val coincideDep = filtroDepartamento == "TODOS" ||
                    CatalogoUbigeoReniec.detectarDepartamento(c.ubigeo).equals(filtroDepartamento, ignoreCase = true) ||
                    c.ubigeo.contains(filtroDepartamento, ignoreCase = true)
                val coincideTexto = q.isBlank() ||
                    c.dni.lowercase().contains(q) ||
                    c.nombreCompleto.lowercase().contains(q) ||
                    c.ubigeo.lowercase().contains(q) ||
                    c.codigoHuellaAfis.lowercase().contains(q)
                coincideDep && coincideTexto
            }
        }
}

class CiudadanoViewModel(
    private val repository: CiudadanoRepository,
    private val listarCiudadanos: ListarCiudadanosUseCase = ListarCiudadanosUseCase(repository),
    private val buscarPorDni: BuscarCiudadanoPorDniUseCase = BuscarCiudadanoPorDniUseCase(repository),
    private val registrarCiudadano: RegistrarCiudadanoUseCase = RegistrarCiudadanoUseCase(repository),
    private val eliminarCiudadano: EliminarCiudadanoUseCase = EliminarCiudadanoUseCase(repository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(CiudadanoUiState())
    val uiState: StateFlow<CiudadanoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            listarCiudadanos().collect { lista ->
                _uiState.update { actual ->
                    actual.copy(
                        ciudadanos = lista,
                        ciudadanoSeleccionado = actual.ciudadanoSeleccionado?.let { sel ->
                            lista.firstOrNull { it.id == sel.id || it.dni == sel.dni }
                        } ?: lista.firstOrNull()
                    )
                }
            }
        }
        sincronizar()
    }

    fun sincronizar() {
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true, mensajeError = null) }
            val res = resultadoDe { repository.sincronizar() }
            _uiState.update {
                it.copy(
                    sincronizando = false,
                    avisoRed = res.exceptionOrNull()?.let { "Modo Brigada Offline: mostrando padrón local SQLDelight" }
                )
            }
        }
    }

    fun onFiltroChange(texto: String) {
        _uiState.update { it.copy(filtroDniONombre = texto, mensajeExito = null, mensajeError = null) }
    }

    fun onFiltroDepartamentoChange(departamento: String) {
        _uiState.update {
            val nuevaLista = it.copy(filtroDepartamento = departamento).ciudadanosFiltrados
            it.copy(
                filtroDepartamento = departamento,
                ciudadanoSeleccionado = nuevaLista.firstOrNull() ?: it.ciudadanoSeleccionado,
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun seleccionarCiudadano(ciudadano: Ciudadano) {
        _uiState.update { it.copy(ciudadanoSeleccionado = ciudadano, mensajeExito = null) }
    }

    fun buscarPorHuellaBiometrica(ciudadano: Ciudadano) {
        _uiState.update {
            it.copy(
                filtroDepartamento = "TODOS",
                filtroDniONombre = ciudadano.dni,
                ciudadanoSeleccionado = ciudadano,
                mensajeExito = "Huella AFIS (${ciudadano.codigoHuellaAfis}) identificada: ${ciudadano.nombreCompleto} - DNI ${ciudadano.dni}",
                mensajeError = null
            )
        }
    }

    fun consultarDniExacto() {
        val dni = _uiState.value.filtroDniONombre.trim()
        viewModelScope.launch {
            buscarPorDni(dni).fold(
                onSuccess = { encontrado ->
                    if (encontrado != null) {
                        _uiState.update {
                            it.copy(
                                ciudadanoSeleccionado = encontrado,
                                mensajeExito = "Consulta PIDE / API RENIEC exitosa: DNI ${encontrado.dni} (${encontrado.ubigeo})",
                                mensajeError = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(mensajeError = "No se encontró el DNI $dni en el padrón nacional")
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(mensajeError = error.message) }
                }
            )
        }
    }

    fun abrirNuevoCiudadano() {
        _uiState.update {
            it.copy(
                editandoId = null,
                mostrarFormulario = true,
                formulario = FormularioCiudadano(huellaCapturada = false),
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
                    donacionOrganos = ciudadano.donacionOrganos,
                    huellaCapturada = true
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

    fun guardarCiudadano() {
        val estado = _uiState.value
        if (estado.guardando) return
        val form = estado.formulario

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
                            formulario = FormularioCiudadano(),
                            mensajeExito = "Ciudadano ${guardado.nombreCompleto} (DNI ${guardado.dni}) registrado con Huella ${guardado.codigoHuellaAfis}"
                        )
                    }
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

    fun eliminar(id: Long) {
        viewModelScope.launch {
            eliminarCiudadano(id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(mensajeExito = "Registro de ciudadano eliminado del padrón")
                    }
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
