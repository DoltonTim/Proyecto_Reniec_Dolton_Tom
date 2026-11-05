package pe.edu.upeu.reniecmovil.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.edu.upeu.reniecmovil.data.repository.AuthRepositorioEnMemoria
import pe.edu.upeu.reniecmovil.data.repository.CiudadanoRepositorioEnMemoria
import pe.edu.upeu.reniecmovil.data.repository.TramiteRepositorioEnMemoria
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
    single<CiudadanoRepository> { CiudadanoRepositorioEnMemoria() }
    single<TramiteRepository> { TramiteRepositorioEnMemoria() }
    single<AuthRepository> { AuthRepositorioEnMemoria() }
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
