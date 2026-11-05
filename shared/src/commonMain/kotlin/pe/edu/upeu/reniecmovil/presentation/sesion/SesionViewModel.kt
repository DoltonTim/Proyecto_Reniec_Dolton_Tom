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
    val usuario: String = "admin",
    val clave: String = "Admin123!",
    val cargando: Boolean = false,
    val sesion: Sesion? = null,
    val error: String? = null
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
            _estado.update { it.copy(error = "Ingrese su código de registrador y contraseña") }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            iniciarSesionUseCase(Credenciales(usuario, clave))
                .onSuccess { sesion ->
                    _estado.update {
                        it.copy(
                            sesion = sesion,
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
                            error = fallo.message ?: "No se pudo autenticar con Central RENIEC"
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
