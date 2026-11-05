package pe.edu.upeu.reniecmovil.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.edu.upeu.reniecmovil.data.local.AlmacenSeguro
import pe.edu.upeu.reniecmovil.data.local.ReniecLocalDataSource
import pe.edu.upeu.reniecmovil.data.remote.CiudadanoApi
import pe.edu.upeu.reniecmovil.data.remote.TramiteApi
import pe.edu.upeu.reniecmovil.data.remote.crearHttpClient
import pe.edu.upeu.reniecmovil.data.repository.AuthRepositorioRest
import pe.edu.upeu.reniecmovil.data.repository.CiudadanoRepositorioOffline
import pe.edu.upeu.reniecmovil.data.repository.TramiteRepositorioOffline
import pe.edu.upeu.reniecmovil.db.ReniecDb
import pe.edu.upeu.reniecmovil.domain.repository.AuthRepository
import pe.edu.upeu.reniecmovil.domain.repository.CiudadanoRepository
import pe.edu.upeu.reniecmovil.domain.repository.TramiteRepository
import pe.edu.upeu.reniecmovil.domain.usecase.BuscarCiudadanoPorDniUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.EliminarCiudadanoUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.EliminarTramiteUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.GuardarTramiteUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.ListarCiudadanosUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.ListarTramitesUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.ObtenerSesionGuardadaUseCase
import pe.edu.upeu.reniecmovil.domain.usecase.RegistrarCiudadanoUseCase
import pe.edu.upeu.reniecmovil.presentation.ciudadano.CiudadanoViewModel
import pe.edu.upeu.reniecmovil.presentation.sesion.SesionViewModel
import pe.edu.upeu.reniecmovil.presentation.tramite.TramiteViewModel

val dataModule = module {
    single { ReniecDb(get()) }
    single { ReniecLocalDataSource(get()) }
    single<HttpClient> { crearHttpClient(get<HttpClientEngine>(), get<AlmacenSeguro>()) }
    single { CiudadanoApi(get()) }
    single { TramiteApi(get()) }
    single<CiudadanoRepository> { CiudadanoRepositorioOffline(get(), get()) }
    single<TramiteRepository> { TramiteRepositorioOffline(get(), get()) }
    single<AuthRepository> { AuthRepositorioRest(get(), get()) }
}

val domainModule = module {
    factory { ListarCiudadanosUseCase(get()) }
    factory { BuscarCiudadanoPorDniUseCase(get()) }
    factory { RegistrarCiudadanoUseCase(get()) }
    factory { EliminarCiudadanoUseCase(get()) }
    factory { ListarTramitesUseCase(get()) }
    factory { GuardarTramiteUseCase(get()) }
    factory { EliminarTramiteUseCase(get()) }
    factory { IniciarSesionUseCase(get()) }
    factory { ObtenerSesionGuardadaUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }
}

val presentationModule = module {
    viewModel { CiudadanoViewModel(get(), get(), get(), get(), get()) }
    viewModel { TramiteViewModel(get(), get(), get(), get()) }
    viewModel { SesionViewModel(get(), get(), get()) }
}

expect val platformModule: Module

fun initKoin(configuracionAdicional: KoinApplication.() -> Unit = {}) {
    startKoin {
        configuracionAdicional()
        modules(
            dataModule,
            domainModule,
            presentationModule,
            platformModule
        )
    }
}
