package ar.um.econsumo.data

/**
 * Clase que representa la solicitud de autenticación con Google
 * Esta clase se serializa a JSON cuando se envía al backend
 */
data class GoogleAuthRequest(
    val email: String,
    val token: String,
    val serverAuthCode: String? = null
)
