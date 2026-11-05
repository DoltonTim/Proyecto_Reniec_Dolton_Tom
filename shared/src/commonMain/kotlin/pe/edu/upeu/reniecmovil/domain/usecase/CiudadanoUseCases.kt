package pe.edu.upeu.reniecmovil.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.repository.CiudadanoRepository

class ListarCiudadanosUseCase(
    private val repository: CiudadanoRepository
) {
    operator fun invoke(): Flow<List<Ciudadano>> = repository.observarCiudadanos()
}

class BuscarCiudadanoPorDniUseCase(
    private val repository: CiudadanoRepository
) {
    suspend operator fun invoke(dni: String): Result<Ciudadano?> = resultadoDe {
        val limpio = dni.trim()
        require(limpio.length == 8 && limpio.all { it.isDigit() }) {
            "El DNI a consultar debe tener exactamente 8 dígitos numéricos"
        }
        repository.buscarPorDni(limpio)
    }
}

class RegistrarCiudadanoUseCase(
    private val repository: CiudadanoRepository
) {
    suspend operator fun invoke(
        id: Long = 0L,
        dni: String,
        nombres: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        fechaNacimiento: String,
        estadoCivil: String,
        ubigeo: String,
        direccion: String,
        donacionOrganos: Boolean
    ): Result<Ciudadano> = resultadoDe {
        val dniTrim = dni.trim()
        val nombresTrim = nombres.trim()
        val paternoTrim = apellidoPaterno.trim()
        val maternoTrim = apellidoMaterno.trim()
        val fechaTrim = fechaNacimiento.trim()
        val ubigeoTrim = ubigeo.trim()
        val direccionTrim = direccion.trim()

        val errores = ErroresCiudadano(
            dni = when {
                dniTrim.isBlank() -> "El número de DNI es obligatorio"
                dniTrim.length != 8 || !dniTrim.all { it.isDigit() } -> "El DNI debe contener exactamente 8 dígitos numéricos"
                else -> null
            },
            nombres = if (nombresTrim.length < 2) "Ingrese los nombres completos" else null,
            apellidoPaterno = if (paternoTrim.length < 2) "Ingrese el apellido paterno" else null,
            apellidoMaterno = if (maternoTrim.length < 2) "Ingrese el apellido materno" else null,
            fechaNacimiento = if (fechaTrim.isBlank()) "Ingrese la fecha de nacimiento (AAAA-MM-DD)" else null,
            ubigeo = if (ubigeoTrim.length < 3) "Ingrese el departamento / provincia / distrito" else null,
            direccion = if (direccionTrim.length < 4) "Ingrese la dirección domiciliaria actual" else null
        )

        if (errores.hayErrores) {
            throw CiudadanoInvalidoException(errores)
        }

        val ciudadano = Ciudadano(
            id = id,
            dni = dniTrim,
            nombres = nombresTrim.uppercase(),
            apellidoPaterno = paternoTrim.uppercase(),
            apellidoMaterno = maternoTrim.uppercase(),
            fechaNacimiento = fechaTrim,
            estadoCivil = estadoCivil,
            ubigeo = ubigeoTrim,
            direccion = direccionTrim,
            donacionOrganos = donacionOrganos
        )

        if (id == 0L) {
            repository.registrar(ciudadano)
        } else {
            repository.actualizar(ciudadano)
        }
    }
}

class EliminarCiudadanoUseCase(
    private val repository: CiudadanoRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> = resultadoDe {
        repository.eliminar(id)
    }
}
