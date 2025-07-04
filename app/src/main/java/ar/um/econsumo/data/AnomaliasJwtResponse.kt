package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del endpoint para obtener todas las anomalías con JWT
 */
data class AnomaliasJwtResponse(
    val nic: String,
    val usuario: String,
    val anomalias: List<AnomaliaInfo>,
    val resumen: ResumenAnomalias,
    val estado: String,
    val mensaje: String? = null
)

data class ResumenAnomalias(
    @SerializedName("total_registros") val totalRegistros: Int,
    @SerializedName("total_anomalias") val totalAnomalias: Int,
    @SerializedName("consumos_normales") val consumosNormales: Int,
    @SerializedName("porcentaje_anomalias") val porcentajeAnomalias: Double,
    @SerializedName("periodo_analizado") val periodoAnalizado: String
)
