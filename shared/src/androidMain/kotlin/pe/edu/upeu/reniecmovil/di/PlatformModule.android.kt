package pe.edu.upeu.reniecmovil.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.reniecmovil.data.local.AlmacenSeguro
import pe.edu.upeu.reniecmovil.data.local.AlmacenSeguroAndroid
import pe.edu.upeu.reniecmovil.db.ReniecDb

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
    single<SqlDriver> {
        AndroidSqliteDriver(ReniecDb.Schema, androidContext(), "reniec_movil.db")
    }
}
