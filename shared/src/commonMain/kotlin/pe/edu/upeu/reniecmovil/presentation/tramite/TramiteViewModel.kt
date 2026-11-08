package pe.edu.upeu.reniecmovil.presentation.tramite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.reniecmovil.data.remote.AuditoriaApi
import pe.edu.upeu.reniecmovil.data.remote.dto.AuditoriaPayloadDto
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.EstadoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.TipoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni
import pe.edu.upeu.reniecmovil.domain.repository.TramiteRepository
import pe.edu.upeu.reniecmovil.domain.usecase.BuscarCiudadanoPorDniUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.EliminarTramiteUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.GuardarTramiteUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.ListarTramitesUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.TramiteInvalidoException
import pe.edu.upeu.reniecmovil.domain.usecase.resultadoDe
import pe.edu.upeu.reniecmovil.obtenerInfoDispositivo

data class FormularioTramite(
    val dniCiudadano: String = "",
    val nombreCiudadano: String = "",
    val tipoTramite: String = TipoTramiteDni.RENOVACION.name,
    val estado: String = EstadoTramiteDni.PENDIENTE.name,
    val ubigeoDestino: String = "150118 - LIMA / LIMA / LURIGANCHO-CHOSICA (Agencia RENIEC Ñaña - UPeU)",
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
    val tramiteVoucherActivo: TramiteDni? = null,
    val tramiteBiometriaActivo: TramiteDni? = null,
    val buscandoDniPadron: Boolean = false,
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
    private val eliminarTramite: EliminarTramiteUseCase = EliminarTramiteUseCase(repository),
    private val buscarCiudadanoPorDni: BuscarCiudadanoPorDniUseCase? = null,
    private val auditoriaApi: AuditoriaApi? = null
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

    fun abrirVoucherQr(tramite: TramiteDni?) {
        _uiState.update { it.copy(tramiteVoucherActivo = tramite) }
    }

    fun abrirBiometriaTramite(tramite: TramiteDni?) {
        _uiState.update { it.copy(tramiteBiometriaActivo = tramite) }
    }

    fun confirmarBiometriaYAprobar(
        usuario: String,
        rol: String,
        modo: String,
        puntaje: String
    ) {
        val tramite = _uiState.value.tramiteBiometriaActivo ?: return
        _uiState.update { it.copy(tramiteBiometriaActivo = null) }
        cambiarEstadoRapido(tramite, EstadoTramiteDni.ATENDIDO, usuario, rol)
        registrarAuditoria(
            usuario = usuario,
            rol = rol,
            accion = "ENTREGA_DNI_BIOMETRICA",
            referenciaDni = tramite.dniCiudadano,
            detalle = "Trámite ${tramite.tipoTramite} aprobado con $modo ($puntaje) para ${tramite.nombreCiudadano}",
            canal = "Google Analytics + Clarity + Píxel TikTok"
        )
    }

    fun registrarAuditoria(
        usuario: String,
        rol: String,
        accion: String,
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
                    entidad = "TRAMITE",
                    referenciaDni = referenciaDni,
                    detalle = detalle,
                    canalTelemetria = canal,
                    dispositivo = "${disp.nombrePlataforma} (${disp.modeloEquipo})"
                )
            )
        }
    }

    fun autocompletarDesdePadron50k(dni: String) {
        val limpio = dni.trim()
        if (limpio.length != 8 || !limpio.all { it.isDigit() } || buscarCiudadanoPorDni == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(buscandoDniPadron = true, mensajeError = null) }
            buscarCiudadanoPorDni.invoke(limpio).fold(
                onSuccess = { ciudadano ->
                    if (ciudadano != null) {
                        _uiState.update { actual ->
                            actual.copy(
                                buscandoDniPadron = false,
                                formulario = actual.formulario.copy(
                                    dniCiudadano = ciudadano.dni,
                                    nombreCiudadano = ciudadano.nombreCompleto,
                                    ubigeoDestino = ciudadano.ubigeo,
                                    dniError = null,
                                    nombreError = null
                                ),
                                mensajeExito = "✓ Datos autocompletados desde Padrón Nacional (+50,000): ${ciudadano.nombreCompleto}"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                buscandoDniPadron = false,
                                mensajeError = "El DNI $limpio no existe en el Padrón (+50,000). Empadrónalo primero."
                            )
                        }
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(buscandoDniPadron = false) }
                }
            )
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
                    ubigeoDestino = ciudadanoPreseleccionado?.ubigeo ?: "150118 - LIMA / LIMA / LURIGANCHO-CHOSICA"
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
        val previoDni = _uiState.value.formulario.dniCiudadano
        _uiState.update {
            it.copy(
                formulario = transformar(it.formulario),
                mensajeExito = null,
                mensajeError = null
            )
        }
        val nuevoDni = _uiState.value.formulario.dniCiudadano
        if (nuevoDni != previoDni && nuevoDni.length == 8 && nuevoDni.all { it.isDigit() }) {
            autocompletarDesdePadron50k(nuevoDni)
        }
    }

    fun guardar(usuarioOperador: String = "dolton", rolOperador: String = "ROLE_BRIGADISTA") {
        val actual = _uiState.value
        if (actual.guardando) return
        val form = actual.formulario
        val eraEdicion = actual.editandoId != null

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
                    val accion = if (!eraEdicion) "registrado" else "actualizado"
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mostrarFormulario = false,
                            editandoId = null,
                            tramiteVoucherActivo = tramite,
                            formulario = FormularioTramite(),
                            mensajeExito = "Trámite DNI de ${tramite.nombreCiudadano} $accion con Voucher QR emitido"
                        )
                    }
                    registrarAuditoria(
                        usuario = usuarioOperador,
                        rol = rolOperador,
                        accion = if (eraEdicion) "ACTUALIZAR_TRAMITE" else "NUEVO_TRAMITE_DNI",
                        referenciaDni = tramite.dniCiudadano,
                        detalle = "Trámite ${tramite.tipoTramite} (${tramite.estado}) para ${tramite.nombreCiudadano}",
                        canal = "Google Analytics + Clarity + Píxel Meta"
                    )
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

    fun cambiarEstadoRapido(
        tramite: TramiteDni,
        nuevoEstado: EstadoTramiteDni,
        usuarioOperador: String = "admin",
        rolOperador: String = "ROLE_ADMIN"
    ) {
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
                registrarAuditoria(
                    usuario = usuarioOperador,
                    rol = rolOperador,
                    accion = "APROBAR_TRAMITE_DNI",
                    referenciaDni = tramite.dniCiudadano,
                    detalle = "Cambio de estado a ${nuevoEstado.name} de ${tramite.nombreCiudadano}"
                )
            }
        }
    }

    fun eliminar(id: Long, dni: String = "", usuarioOperador: String = "admin", rolOperador: String = "ROLE_ADMIN") {
        viewModelScope.launch {
            eliminarTramite(id).fold(
                onSuccess = {
                    _uiState.update { it.copy(mensajeExito = "Trámite DNI anulado / eliminado correctamente") }
                    registrarAuditoria(
                        usuario = usuarioOperador,
                        rol = rolOperador,
                        accion = "ANULAR_TRAMITE_DNI",
                        referenciaDni = dni.ifBlank { id.toString() },
                        detalle = "Anulación de trámite ID $id en Central RENIEC"
                    )
                },
                onFailure = { fallo ->
                    _uiState.update { it.copy(mensajeError = fallo.message ?: "Error al eliminar trámite") }
                }
            )
        }
    }
}
