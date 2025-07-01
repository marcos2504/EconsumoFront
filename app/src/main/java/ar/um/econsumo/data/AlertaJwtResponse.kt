package ar.um.econsumo.data

/**
 * Respuesta del endpoint para alertas con JWT
 */
data class AlertaJwtResponse(
    val nic: String,
    val usuario: String,
    val fecha: String? = null,
    val consumo_kwh: Double? = null,
    val anomalia: Int? = null,
    val estado: String? = null,
    val comparado_trimestre: Double? = null,
    val mensaje: String? = null,
    val error: String? = null
)
