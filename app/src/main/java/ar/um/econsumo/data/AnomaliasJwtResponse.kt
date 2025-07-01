package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del endpoint para obtener todas las anomalías con JWT
 */
data class AnomaliasJwtResponse(
    val nic: String,
    val usuario: String,
    val anomalias: List<AnomaliaInfo>,
    @SerializedName("total_anomalias") val totalAnomalias: Int,
    val error: String? = null
)
