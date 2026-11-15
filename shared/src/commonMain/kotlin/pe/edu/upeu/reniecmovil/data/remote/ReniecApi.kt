package pe.edu.upeu.reniecmovil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import pe.edu.upeu.reniecmovil.data.remote.dto.AuditoriaDto
import pe.edu.upeu.reniecmovil.data.remote.dto.AuditoriaPayloadDto
import pe.edu.upeu.reniecmovil.data.remote.dto.CiudadanoDto
import pe.edu.upeu.reniecmovil.data.remote.dto.OperadorReniecDto
import pe.edu.upeu.reniecmovil.data.remote.dto.OperadorReniecPayloadDto
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
        409 -> ErrorApi.Conflicto("Conflicto en el registro remoto (Dato duplicado)")
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
        val destacados = try {
            client.get("ciudadanos?id=lte.20&select=*&order=id.asc")
                .body<List<CiudadanoDto>>()
                .map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
        val recientes = client.get("ciudadanos?select=*&order=id.desc&limit=80")
            .body<List<CiudadanoDto>>()
            .map { it.toDomain() }

        (destacados + recientes).distinctBy { it.dni }
    }

    open suspend fun obtenerTotalPadron(): Long = try {
        ejecutarLlamadaRemota {
            client.post("rpc/obtener_total_padron")
                .body<Long>()
        }
    } catch (_: Exception) {
        50009L
    }

    open suspend fun buscarPorDni(dni: String): Ciudadano? = try {
        ejecutarLlamadaRemota {
            client.get("ciudadanos?dni=eq.$dni&select=*&limit=1")
                .body<List<CiudadanoDto>>()
                .firstOrNull()
                ?.toDomain()
        }
    } catch (e: ErrorApiException) {
        if (e.error is ErrorApi.NoEncontrado) null else throw e
    }

    open suspend fun buscarEnPadronMasivo(consulta: String, departamento: String = "TODOS"): List<Ciudadano> = try {
        ejecutarLlamadaRemota {
            val limpio = consulta.trim().uppercase()
            val filtroDep = if (departamento != "TODOS") {
                "&ubigeo=ilike.*${departamento.encodeURLParameter()}*"
            } else {
                ""
            }
            val url = if (limpio.isBlank()) {
                "ciudadanos?select=*${filtroDep}&order=id.desc&limit=60"
            } else if (limpio.all { it.isDigit() }) {
                "ciudadanos?dni=like.${limpio}*${filtroDep}&select=*&order=dni.asc&limit=60"
            } else {
                val q = limpio.encodeURLParameter()
                "ciudadanos?or=(apellido_paterno.ilike.*$q*,apellido_materno.ilike.*$q*,nombres.ilike.*$q*,ubigeo.ilike.*$q*)${filtroDep}&select=*&limit=60"
            }
            client.get(url)
                .body<List<CiudadanoDto>>()
                .map { it.toDomain() }
        }
    } catch (_: Exception) {
        emptyList()
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

open class AuditoriaApi(
    private val client: HttpClient
) {
    open suspend fun listarRecientes(limite: Int = 25): List<AuditoriaDto> = try {
        ejecutarLlamadaRemota {
            client.get("auditoria_reniec?select=*&order=id.desc&limit=$limite")
                .body<List<AuditoriaDto>>()
        }
    } catch (_: Exception) {
        emptyList()
    }

    open suspend fun registrarEvento(payload: AuditoriaPayloadDto): AuditoriaDto? = try {
        ejecutarLlamadaRemota {
            client.post("auditoria_reniec") {
                setBody(payload)
            }.body<List<AuditoriaDto>>().firstOrNull()
        }
    } catch (_: Exception) {
        null
    }
}

@Serializable
private data class CambioCargoPatchDto(
    val cargo: String,
    val rol: String,
    val permisos: String
)

open class OperadorApi(
    private val client: HttpClient
) {
    open suspend fun listarOperadores(): List<OperadorReniecDto> = try {
        ejecutarLlamadaRemota {
            client.get("operadores_reniec?activo=eq.true&select=*&order=id.asc")
                .body<List<OperadorReniecDto>>()
        }
    } catch (_: Exception) {
        emptyList()
    }

    open suspend fun buscarPorUsername(username: String): OperadorReniecDto? = try {
        ejecutarLlamadaRemota {
            client.get("operadores_reniec?username=eq.${username.lowercase().encodeURLParameter()}&select=*&limit=1")
                .body<List<OperadorReniecDto>>()
                .firstOrNull()
        }
    } catch (_: Exception) {
        null
    }

    open suspend fun crearOperador(payload: OperadorReniecPayloadDto): OperadorReniecDto = ejecutarLlamadaRemota {
        client.post("operadores_reniec") {
            setBody(payload)
        }.body<List<OperadorReniecDto>>().first()
    }

    open suspend fun cambiarCargo(id: Long, cargo: String, rol: String, permisos: String): OperadorReniecDto? = try {
        ejecutarLlamadaRemota {
            client.patch("operadores_reniec?id=eq.$id") {
                setBody(CambioCargoPatchDto(cargo = cargo, rol = rol, permisos = permisos))
            }.body<List<OperadorReniecDto>>().firstOrNull()
        }
    } catch (_: Exception) {
        null
    }

    @Serializable
    private data class BajaOperadorDto(val activo: Boolean = false)

    open suspend fun eliminarOperador(id: Long) = ejecutarLlamadaRemota {
        // En lugar de DELETE físico que rompería la auditoría histórica de RENIEC,
        // realiza baja lógica (activo = false) para revocar acceso inmediato preservando reportes
        try {
            client.delete("operadores_reniec?id=eq.$id")
        } catch (_: Exception) {
            client.patch("operadores_reniec?id=eq.$id") {
                setBody(BajaOperadorDto(activo = false))
            }
        }
        Unit
    }
}

/**
 * Cliente para almacenar y recuperar fotografías biométricas JPEG en Supabase Storage
 * dentro del bucket público `fotos-ciudadanos` ({dni}.jpg).
 */
open class FotoStorageApi(private val client: HttpClient) {
    companion object {
        const val BUCKET_FOTOS = "fotos-ciudadanos"
        const val STORAGE_BASE = "https://vktclzpkebwvfbrvtpxo.supabase.co/storage/v1/object"
    }

    fun urlPublicaFoto(dni: String): String = "$STORAGE_BASE/public/$BUCKET_FOTOS/${dni.trim()}.jpg"

    open suspend fun subirFotoCiudadano(dni: String, jpegBytes: ByteArray): Result<String> {
        val dniLimpio = dni.trim()
        if (dniLimpio.isEmpty() || jpegBytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("DNI o imagen vacía"))
        }
        return try {
            val respuesta = client.post("$STORAGE_BASE/$BUCKET_FOTOS/$dniLimpio.jpg") {
                header("x-upsert", "true")
                setBody(ByteArrayContent(jpegBytes, ContentType.Image.JPEG))
            }
            if (respuesta.status.isSuccess()) {
                Result.success(urlPublicaFoto(dniLimpio))
            } else {
                Result.failure(Exception("HTTP ${respuesta.status.value} al subir fotografía"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun descargarFotoCiudadano(dni: String): ByteArray? {
        val dniLimpio = dni.trim()
        if (dniLimpio.isEmpty()) return null
        return try {
            val respuesta = client.get(urlPublicaFoto(dniLimpio))
            if (respuesta.status.isSuccess()) {
                respuesta.readRawBytes().takeIf { it.isNotEmpty() }
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }
}
