package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del endpoint para consultar el último consumo
 */
data class UltimoConsumoResponse(
    val nic: String,
    val usuario: String,
    val fecha: String?,
    @SerializedName("consumo_kwh") val consumoKwh: Double?,
    @SerializedName("es_anomalia") val esAnomalia: Boolean?,
    @SerializedName("variacion_trimestre") val variacionTrimestre: Double?,
    @SerializedName("score_anomalia") val scoreAnomalia: Double?,
    val mensaje: String?,
    val estado: String
)
