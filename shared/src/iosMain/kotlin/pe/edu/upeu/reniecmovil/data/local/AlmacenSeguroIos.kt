package pe.edu.upeu.reniecmovil.data.local

import platform.Foundation.NSUserDefaults

class AlmacenSeguroIos : AlmacenSeguro {
    private val prefs = NSUserDefaults.standardUserDefaults
    private val keyToken = "reniec_jwt_token"

    override fun guardarToken(token: String) {
        prefs.setObject(token, forKey = keyToken)
    }

    override fun leerToken(): String? = prefs.stringForKey(keyToken)

    override fun borrarToken() {
        prefs.removeObjectForKey(keyToken)
    }
}
