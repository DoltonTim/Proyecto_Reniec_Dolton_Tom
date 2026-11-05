package pe.edu.upeu.reniecmovil

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.reniecmovil.data.local.ReniecLocalDataSource
import pe.edu.upeu.reniecmovil.data.remote.TramiteApi
import pe.edu.upeu.reniecmovil.data.repository.CiudadanoRepositorioEnMemoria
import pe.edu.upeu.reniecmovil.data.repository.TramiteRepositorioOffline
import pe.edu.upeu.reniecmovil.db.ReniecDb
import pe.edu.upeu.reniecmovil.domain.error.ErrorApi
import pe.edu.upeu.reniecmovil.domain.error.ErrorApiException
import pe.edu.upeu.reniecmovil.domain.model.EstadoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.TipoTramiteDni
import pe.edu.upeu.reniecmovil.domain.model.TramiteDni
import pe.edu.upeu.reniecmovil.domain.usecase.BuscarCiudadanoPorDniUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.GuardarTramiteUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.RegistrarCiudadanoUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReniecOfflineAndUseCasesTest {

    private fun crearBaseEnMemoria(): ReniecDb {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ReniecDb.Schema.create(driver)
        return ReniecDb(driver)
    }

    private fun apiTramitesSinConexion(): TramiteApi = object : TramiteApi(HttpClient()) {
        override suspend fun listar(): List<TramiteDni> {
            throw ErrorApiException(ErrorApi.SinConexion)
        }

        override suspend fun registrar(tramite: TramiteDni): TramiteDni {
            throw ErrorApiException(ErrorApi.SinConexion)
        }
    }

    private fun apiTramitesEnMemoria(
        iniciales: List<TramiteDni> = emptyList(),
        eventos: MutableList<String> = mutableListOf()
    ): TramiteApi = object : TramiteApi(HttpClient()) {
        private val servidor = iniciales.toMutableList()
        private var siguienteId = (servidor.maxOfOrNull { it.id } ?: 0L) + 1L

        override suspend fun listar(): List<TramiteDni> {
            eventos.add("listar")
            return servidor.toList()
        }

        override suspend fun registrar(tramite: TramiteDni): TramiteDni {
            eventos.add("registrar:${tramite.dniCiudadano}")
            val creado = tramite.copy(id = siguienteId++, pendienteSync = false)
            servidor.add(creado)
            return creado
        }
    }

    @Test
    fun `validar y buscar ciudadano por DNI de 8 digitos en padron RENIEC`() = runTest {
        val repo = CiudadanoRepositorioEnMemoria()
        val buscarUseCase = BuscarCiudadanoPorDniUseCase(repo)

        val encontrado = buscarUseCase("72345678").getOrNull()
        assertNotNull(encontrado)
        assertEquals("DOLTON TOM", encontrado.nombres)

        val dniInvalido = buscarUseCase("123")
        assertTrue(dniInvalido.isFailure)
    }

    @Test
    fun `registrar ciudadano rechaza DNI que no tenga 8 digitos numericos`() = runTest {
        val repo = CiudadanoRepositorioEnMemoria()
        val registrarUseCase = RegistrarCiudadanoUseCase(repo)

        val resultado = registrarUseCase(
            dni = "7234ABCD",
            nombres = "LUIS",
            apellidoPaterno = "RAMOS",
            apellidoMaterno = "DIAZ",
            fechaNacimiento = "2000-01-01",
            estadoCivil = "SOLTERO",
            ubigeo = "LIMA / LIMA / ATE",
            direccion = "AV. CENTRAL 123",
            donacionOrganos = true
        )

        assertTrue(resultado.isFailure)
    }

    @Test
    fun `sin red el tramite DNI se guarda en SQLDelight como pendiente con id negativo`() = runTest {
        val local = ReniecLocalDataSource(crearBaseEnMemoria(), cargarSemillaSiVacio = false)
        val repoOffline = TramiteRepositorioOffline(local, apiTramitesSinConexion())
        val guardarUseCase = GuardarTramiteUseCase(repoOffline)

        val creado = guardarUseCase(
            dniCiudadano = "72345678",
            nombreCiudadano = "DOLTON TOM MEZA QUISPE",
            tipoTramite = TipoTramiteDni.DUPLICADO.name,
            estado = EstadoTramiteDni.PENDIENTE.name,
            ubigeoDestino = "LIMA / LURIGANCHO - AGENCIA NANA",
            observaciones = "Registro en brigada sin internet"
        ).getOrThrow()

        assertTrue(creado.id < 0, "El trámite guardado sin conexión debe tener ID local negativo")
        assertTrue(creado.pendienteSync, "El trámite debe quedar marcado como pendiente de sincronizar")

        val enLista = repoOffline.observarTramites().first()
        assertEquals(1, enLista.size)
        assertEquals("72345678", enLista.single().dniCiudadano)
    }

    @Test
    fun `al recuperar conexion sincroniza tramites pendientes hacia el backend REST y asigna id positivo`() = runTest {
        val local = ReniecLocalDataSource(crearBaseEnMemoria(), cargarSemillaSiVacio = false)
        val eventos = mutableListOf<String>()

        // 1. Brigada sin señal registra trámite offline
        val repoOffline = TramiteRepositorioOffline(local, apiTramitesSinConexion())
        repoOffline.registrar(
            TramiteDni(
                dniCiudadano = "45896321",
                nombreCiudadano = "MARIA ELENA PAREDES MENDOZA",
                tipoTramite = TipoTramiteDni.RENOVACION.name,
                estado = EstadoTramiteDni.PENDIENTE.name,
                ubigeoDestino = "JUNIN / HUANCAYO",
                observaciones = "Renovación registrada en campaña rural"
            )
        )
        assertEquals(1, local.tramitesPendientes().size)

        // 2. Regresa la conexión a Central RENIEC y se ejecuta sincronizar()
        val repoOnline = TramiteRepositorioOffline(
            local = local,
            remoto = apiTramitesEnMemoria(eventos = eventos)
        )
        repoOnline.sincronizar()

        assertEquals(listOf("registrar:45896321", "listar"), eventos)
        assertTrue(local.tramitesPendientes().isEmpty())

        val listaFinal = repoOnline.observarTramites().first()
        assertEquals(1, listaFinal.size)
        assertTrue(listaFinal.single().id > 0)
    }
}
