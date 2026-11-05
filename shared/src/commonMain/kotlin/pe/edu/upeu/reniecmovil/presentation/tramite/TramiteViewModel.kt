package pe.edu.upeu.reniecmovil.presentation.tramite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.EstadoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.TipoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni
import pe.edu.upeu.reniecmovil.domain.repository.TramiteRepository
import pe.edu.upeu.reniecmovil.domain.usecase.EliminarTramiteUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.GuardarTramiteUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.ListarTramitesUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.TramiteInvalidoException
import pe.edu.upeu.reniecmovil.domain.usecase.resultadoDe

data class FormularioTramite(
    val dniCiudadano: String = "",
    val nombreCiudadano: String = "",
    val tipoTramite: String = TipoTramiteDni.RENOVACION.name,
    val estado: String = EstadoTramiteDni.PENDIENTE.name,
    val ubigeoDestino: String = "LIMA / LURIGANCHO - AGENCIA ÑAÑA",
    val observaciones: String = "",
    val dniError: String? = null,
    val nombreError: String? = null,
    val ubigeoError: String? = null,
    val observacionesError: String? = null
)

data class TramiteUiState(
    val tramites: List<TramiteDni> = emptyList(),
    val filtroBusqueda: String = "",
    val filtroEstado: String = "TODOS",
    val editandoId: Long? = null,
    val mostrarFormulario: Boolean = false,
    val formulario: FormularioTramite = FormularioTramite(),
    val sincronizando: Boolean = false,
    val guardando: Boolean = false,
    val avisoRed: String? = null,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
) {
    val pendientesOffline: Int
        get() = tramites.count { it.pendienteSync || it.id < 0 }

    val tramitesFiltrados: List<TramiteDni>
        get() {
            val q = filtroBusqueda.trim().lowercase()
            return tramites.filter { tramite ->
                val coincideEstado = filtroEstado == "TODOS" || tramite.estado.equals(filtroEstado, ignoreCase = true)
                val coincideTexto = q.isBlank() ||
                    tramite.dniCiudadano.lowercase().contains(q) ||
                    tramite.nombreCiudadano.lowercase().contains(q) ||
                    tramite.tipoTramite.lowercase().contains(q) ||
                    tramite.ubigeoDestino.lowercase().contains(q)
                coincideEstado && coincideTexto
            }
        }
}

class TramiteViewModel(
    private val repository: TramiteRepository,
    private val listarTramites: ListarTramitesUseCase = ListarTramitesUseCase(repository),
    private val guardarTramite: GuardarTramiteUseCase = GuardarTramiteUseCase(repository),
    private val eliminarTramite: EliminarTramiteUseCase = EliminarTramiteUseCase(repository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(TramiteUiState())
    val uiState: StateFlow<TramiteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            listarTramites().collect { lista ->
                _uiState.update { it.copy(tramites = lista) }
            }
        }
        sincronizar()
    }

    fun sincronizar() {
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true, mensajeError = null) }
            val resultado = resultadoDe { repository.sincronizar() }
            _uiState.update {
                it.copy(
                    sincronizando = false,
                    avisoRed = resultado.exceptionOrNull()?.let {
                        "Sin conexión con Central RENIEC: operando con base local Offline-First"
                    },
                    mensajeExito = if (resultado.isSuccess) "Sincronización con Central RENIEC completada" else it.mensajeExito
                )
            }
        }
    }

    fun onFiltroBusquedaChange(texto: String) {
        _uiState.update { it.copy(filtroBusqueda = texto, mensajeExito = null) }
    }

    fun onFiltroEstadoChange(estado: String) {
        _uiState.update { it.copy(filtroEstado = estado, mensajeExito = null) }
    }

    fun abrirNuevoTramite(ciudadanoPreseleccionado: Ciudadano? = null) {
        _uiState.update {
            it.copy(
                editandoId = null,
                mostrarFormulario = true,
                formulario = FormularioTramite(
                    dniCiudadano = ciudadanoPreseleccionado?.dni.orEmpty(),
                    nombreCiudadano = ciudadanoPreseleccionado?.nombreCompleto.orEmpty(),
                    ubigeoDestino = ciudadanoPreseleccionado?.ubigeo ?: "LIMA / LURIGANCHO - AGENCIA ÑAÑA"
                ),
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun iniciarEdicion(tramite: TramiteDni) {
        _uiState.update {
            it.copy(
                editandoId = tramite.id,
                mostrarFormulario = true,
                formulario = FormularioTramite(
                    dniCiudadano = tramite.dniCiudadano,
                    nombreCiudadano = tramite.nombreCiudadano,
                    tipoTramite = tramite.tipoTramite,
                    estado = tramite.estado,
                    ubigeoDestino = tramite.ubigeoDestino,
                    observaciones = tramite.observaciones
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
                formulario = FormularioTramite()
            )
        }
    }

    fun actualizarFormulario(transformar: (FormularioTramite) -> FormularioTramite) {
        _uiState.update {
            it.copy(
                formulario = transformar(it.formulario),
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun guardar() {
        val actual = _uiState.value
        if (actual.guardando) return
        val form = actual.formulario

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null, mensajeExito = null) }
            guardarTramite(
                id = actual.editandoId ?: 0L,
                dniCiudadano = form.dniCiudadano,
                nombreCiudadano = form.nombreCiudadano,
                tipoTramite = form.tipoTramite,
                estado = form.estado,
                ubigeoDestino = form.ubigeoDestino,
                observaciones = form.observaciones
            ).fold(
                onSuccess = { tramite ->
                    val accion = if (actual.editandoId == null) "registrado" else "actualizado"
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mostrarFormulario = false,
                            editandoId = null,
                            formulario = FormularioTramite(),
                            mensajeExito = "Trámite DNI de ${tramite.nombreCiudadano} $accion correctamente"
                        )
                    }
                },
                onFailure = { fallo ->
                    when (fallo) {
                        is TramiteInvalidoException -> _uiState.update {
                            it.copy(
                                guardando = false,
                                formulario = it.formulario.copy(
                                    dniError = fallo.errores.dniCiudadano,
                                    nombreError = fallo.errores.nombreCiudadano,
                                    ubigeoError = fallo.errores.ubigeoDestino,
                                    observacionesError = fallo.errores.observaciones
                                )
                            )
                        }
                        else -> _uiState.update {
                            it.copy(
                                guardando = false,
                                mensajeError = fallo.message ?: "No se pudo guardar el trámite"
                            )
                        }
                    }
                }
            )
        }
    }

    fun cambiarEstadoRapido(tramite: TramiteDni, nuevoEstado: EstadoTramiteDni) {
        viewModelScope.launch {
            guardarTramite(
                id = tramite.id,
                dniCiudadano = tramite.dniCiudadano,
                nombreCiudadano = tramite.nombreCiudadano,
                tipoTramite = tramite.tipoTramite,
                estado = nuevoEstado.name,
                ubigeoDestino = tramite.ubigeoDestino,
                observaciones = tramite.observaciones,
                fechaRegistro = tramite.fechaRegistro
            ).onSuccess {
                _uiState.update {
                    it.copy(mensajeExito = "Estado del trámite DNI ${tramite.dniCiudadano} cambiado a ${nuevoEstado.etiqueta}")
                }
            }
        }
    }

    fun eliminar(id: Long) {
        viewModelScope.launch {
            eliminarTramite(id).fold(
                onSuccess = {
                    _uiState.update { it.copy(mensajeExito = "Trámite DNI anulado / eliminado correctamente") }
                },
                onFailure = { fallo ->
                    _uiState.update { it.copy(mensajeError = fallo.message ?: "Error al eliminar trámite") }
                }
            )
        }
    }
}
