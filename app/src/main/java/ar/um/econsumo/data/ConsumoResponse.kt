package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del endpoint principal para consultar consumo y anomalías
 */
data class ConsumoResponse(
    val nic: String,
    val usuario: String,
    @SerializedName("alerta_actual") val alertaActual: AlertaInfo,
    @SerializedName("anomalias_historicas") val anomaliasHistoricas: List<AnomaliaInfo>,
    val resumen: ResumenConsumo
)

data class ResumenConsumo(
    @SerializedName("tiene_anomalia_actual") val tieneAnomaliaActual: Boolean,
    @SerializedName("total_anomalias") val totalAnomalias: Int,
    @SerializedName("ultimo_consumo") val ultimoConsumo: Double?,
    @SerializedName("fecha_ultimo") val fechaUltimo: String?,
    @SerializedName("variacion_porcentual") val variacionPorcentual: Double?
)

// La clase AlertaInfo está ahora en su propio archivo
// La clase AnomaliaInfo está ahora en su propio archivo
