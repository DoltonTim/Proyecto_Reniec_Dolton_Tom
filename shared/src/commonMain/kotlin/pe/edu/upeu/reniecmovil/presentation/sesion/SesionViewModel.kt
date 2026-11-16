package pe.edu.upeu.reniecmovil.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.reniecmovil.domain.model.Credenciales
import pe.edu.upeu.reniecmovil.domain.model.Sesion
import pe.edu.upeu.reniecmovil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.ObtenerSesionGuardadaUseCase

data class SesionUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val sesion: Sesion? = null,
    val error: String? = null,
    val enviandoOtp: Boolean = false,
    val codigoOtpEnviado: Boolean = false,
    val mensajeOtp: String? = null,
    val errorOtp: String? = null
)

class SesionViewModel(
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val obtenerSesionGuardadaUseCase: ObtenerSesionGuardadaUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(SesionUiState())
    val estado: StateFlow<SesionUiState> = _estado.asStateFlow()

    init {
        verificarSesion()
    }

    fun onUsuarioChange(usuario: String) {
        _estado.update { it.copy(usuario = usuario, error = null) }
    }

    fun onClaveChange(clave: String) {
        _estado.update { it.copy(clave = clave, error = null) }
    }

    fun limpiarEstadoOtp() {
        _estado.update {
            it.copy(
                enviandoOtp = false,
                codigoOtpEnviado = false,
                mensajeOtp = null,
                errorOtp = null
            )
        }
    }

    fun enviarCodigoCorreoGoogle(usuario: String, correo: String, telefono: String) {
        viewModelScope.launch {
            _estado.update { it.copy(enviandoOtp = true, errorOtp = null, mensajeOtp = null) }
            iniciarSesionUseCase.enviarCodigoValidado(usuario, correo, telefono)
                .onSuccess {
                    _estado.update {
                        it.copy(
                            enviandoOtp = false,
                            codigoOtpEnviado = true,
                            mensajeOtp = "✓ Identidad validada (Usuario + Teléfono). Código de 6 dígitos enviado a ${correo.trim()} — revisa tu Gmail o Spam.",
                            errorOtp = null
                        )
                    }
                }
                .onFailure { fallo ->
                    _estado.update {
                        it.copy(
                            enviandoOtp = false,
                            errorOtp = fallo.message ?: "No se pudo enviar el código. Espera 60 segundos antes de reintentar."
                        )
                    }
                }
        }
    }

    fun verificarCodigoCorreoGoogle(
        usuario: String,
        correo: String,
        codigo: String,
        nuevaClave: String,
        onExito: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _estado.update { it.copy(enviandoOtp = true, errorOtp = null) }
            iniciarSesionUseCase.verificarYCambiarClave(usuario, correo, codigo, nuevaClave)
                .onSuccess { sesion ->
                    _estado.update {
                        it.copy(
                            sesion = sesion,
                            enviandoOtp = false,
                            codigoOtpEnviado = false,
                            mensajeOtp = null,
                            errorOtp = null
                        )
                    }
                    onExito()
                }
                .onFailure { fallo ->
                    _estado.update {
                        it.copy(
                            enviandoOtp = false,
                            errorOtp = fallo.message ?: "Código inválido o expirado"
                        )
                    }
                }
        }
    }

    fun verificarSesion() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            val guardada = obtenerSesionGuardadaUseCase()
            _estado.update { it.copy(sesion = guardada, cargando = false) }
        }
    }

    fun iniciarSesion(onExito: () -> Unit = {}) {
        val usuario = _estado.value.usuario.trim()
        val clave = _estado.value.clave.trim()

        if (usuario.isBlank() || clave.isBlank()) {
            _estado.update { it.copy(error = "Ingrese su usuario y contraseña institucional") }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            iniciarSesionUseCase(Credenciales(usuario, clave))
                .onSuccess { sesion ->
                    _estado.update {
                        it.copy(
                            sesion = sesion,
                            usuario = "",
                            clave = "",
                            cargando = false,
                            error = null
                        )
                    }
                    onExito()
                }
                .onFailure { fallo ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            error = fallo.message ?: "Credenciales inválidas"
                        )
                    }
                }
        }
    }

    fun cerrarSesion(onCerrar: () -> Unit = {}) {
        viewModelScope.launch {
            cerrarSesionUseCase()
            _estado.update { SesionUiState() }
            onCerrar()
        }
    }
}
