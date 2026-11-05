package pe.edu.upeu.reniecmovil.data.local

class AlmacenSeguroIos : AlmacenSeguro {
    private var tokenAlmacenado: String? = null

    override fun guardarToken(token: String) {
        tokenAlmacenado = token
    }

    override fun leerToken(): String? = tokenAlmacenado

    override fun borrarToken() {
        tokenAlmacenado = null
    }
}
