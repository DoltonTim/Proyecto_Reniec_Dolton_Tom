package pe.edu.upeu.reniecmovil.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.MasterKey

class AlmacenSeguroAndroid(contexto: Context) : AlmacenSeguro {

    private val llave = MasterKey.Builder(contexto)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        contexto,
        "reniec_sesion_segura",
        llave,
        PrefKeyEncryptionScheme.AES256_SIV,
        PrefValueEncryptionScheme.AES256_GCM
    )

    override fun guardarToken(token: String) = prefs.edit().putString(CLAVE, token).apply()

    override fun leerToken(): String? = prefs.getString(CLAVE, null)

    override fun borrarToken() = prefs.edit().remove(CLAVE).apply()

    private companion object {
        const val CLAVE = "jwt_reniec"
    }
}
