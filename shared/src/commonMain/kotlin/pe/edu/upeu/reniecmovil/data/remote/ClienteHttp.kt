package pe.edu.upeu.reniecmovil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.edu.upeu.reniecmovil.data.local.AlmacenSeguro

const val URL_BASE_RENIEC = "https://vktclzpkebwvfbrvtpxo.supabase.co/rest/v1/"
const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_R4lNuJJ9kfF9LGuUlbLLgA_MkQIJQ5B"

fun crearHttpClient(
    motor: HttpClientEngine,
    almacen: AlmacenSeguro,
    esDepuracion: Boolean = true
): HttpClient = HttpClient(motor) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
        })
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 10000
        connectTimeoutMillis = 6000
    }

    defaultRequest {
        url(URL_BASE_RENIEC)
        contentType(ContentType.Application.Json)
        header("apikey", SUPABASE_PUBLISHABLE_KEY)
        header("Prefer", "return=representation")
    }

    install(Logging) {
        logger = Logger.SIMPLE
        level = if (esDepuracion) LogLevel.HEADERS else LogLevel.NONE
        sanitizeHeader { nombre -> nombre == HttpHeaders.Authorization || nombre == "apikey" }
    }
}
