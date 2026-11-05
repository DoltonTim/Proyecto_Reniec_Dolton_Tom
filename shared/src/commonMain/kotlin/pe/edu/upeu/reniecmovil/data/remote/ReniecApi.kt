package pe.edu.upeu.reniecmovil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.reniecmovil.data.remote.dto.CiudadanoDto
import pe.edu.upeu.reniecmovil.data.remote.dto.TramiteDto
import pe.edu.upeu.reniecmovil.data.remote.dto.toPayloadDto
import pe.edu.upeu.reniecmovil.domain.error.ErrorApi
import pe.edu.upeu.reniecmovil.domain.error.ErrorApiException
import pe.edu.upeu.reniecmovil.domain.model.Ciudadano
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni

suspend fun <T> ejecutarLlamadaRemota(bloque: suspend () -> T): T = try {
    bloque()
} catch (cancelacion: CancellationException) {
    throw cancelacion
} catch (e: ClientRequestException) {
    val error = when (e.response.status.value) {
        404 -> ErrorApi.NoEncontrado
        409 -> ErrorApi.Conflicto("Conflicto en el registro remoto (DNI duplicado)")
        else -> ErrorApi.Servidor
    }
    throw ErrorApiException(error)
} catch (e: ServerResponseException) {
    throw ErrorApiException(ErrorApi.Servidor)
} catch (e: HttpRequestTimeoutException) {
    throw ErrorApiException(ErrorApi.TiempoAgotado)
} catch (e: Exception) {
    throw ErrorApiException(ErrorApi.SinConexion)
}

open class CiudadanoApi(
    private val client: HttpClient
) {
    open suspend fun listar(): List<Ciudadano> = ejecutarLlamadaRemota {
        client.get("ciudadanos?select=*&order=id.desc")
            .body<List<CiudadanoDto>>()
            .map { it.toDomain() }
    }

    open suspend fun buscarPorDni(dni: String): Ciudadano? = try {
        ejecutarLlamadaRemota {
            client.get("ciudadanos?dni=eq.$dni&select=*")
                .body<List<CiudadanoDto>>()
                .firstOrNull()
                ?.toDomain()
        }
    } catch (e: ErrorApiException) {
        if (e.error is ErrorApi.NoEncontrado) null else throw e
    }

    open suspend fun registrar(ciudadano: Ciudadano): Ciudadano = ejecutarLlamadaRemota {
        client.post("ciudadanos") {
            setBody(ciudadano.toPayloadDto())
        }.body<List<CiudadanoDto>>().first().toDomain()
    }

    open suspend fun actualizar(ciudadano: Ciudadano): Ciudadano = ejecutarLlamadaRemota {
        client.patch("ciudadanos?id=eq.${ciudadano.id}") {
            setBody(ciudadano.toPayloadDto())
        }.body<List<CiudadanoDto>>().firstOrNull()?.toDomain() ?: ciudadano
    }

    open suspend fun eliminar(id: Long) = ejecutarLlamadaRemota {
        client.delete("ciudadanos?id=eq.$id")
        Unit
    }
}

open class TramiteApi(
    private val client: HttpClient
) {
    open suspend fun listar(): List<TramiteDni> = ejecutarLlamadaRemota {
        client.get("tramites?select=*&order=id.desc")
            .body<List<TramiteDto>>()
            .map { it.toDomain() }
    }

    open suspend fun registrar(tramite: TramiteDni): TramiteDni = ejecutarLlamadaRemota {
        client.post("tramites") {
            setBody(tramite.toPayloadDto())
        }.body<List<TramiteDto>>().first().toDomain()
    }

    open suspend fun actualizar(tramite: TramiteDni): TramiteDni = ejecutarLlamadaRemota {
        client.patch("tramites?id=eq.${tramite.id}") {
            setBody(tramite.toPayloadDto())
        }.body<List<TramiteDto>>().firstOrNull()?.toDomain() ?: tramite
    }

    open suspend fun eliminar(id: Long) = ejecutarLlamadaRemota {
        client.delete("tramites?id=eq.$id")
        Unit
    }
}
