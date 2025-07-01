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

data class AlertaInfo(
    val nic: String? = null,
    val fecha: String? = null,
    @SerializedName("consumo_kwh") val consumoKwh: Double? = null,
    val anomalia: Boolean? = null,
    val estado: String? = null,
    @SerializedName("comparado_trimestre") val comparadoTrimestre: Double? = null,
    val mensaje: String? = null
)

data class AnomaliaInfo(
    val fecha: String? = null,
    @SerializedName("consumo_kwh") val consumoKwh: Double? = null,
    val anomalia: Int? = null,
    @SerializedName("comparado_trimestre") val comparadoTrimestre: Double? = null,
    val mensaje: String? = null
)

data class ResumenConsumo(
    @SerializedName("tiene_anomalia_actual") val tieneAnomaliaActual: Boolean,
    @SerializedName("total_anomalias") val totalAnomalias: Int,
    @SerializedName("ultimo_consumo") val ultimoConsumo: Double?,
    @SerializedName("fecha_ultimo") val fechaUltimo: String?,
    @SerializedName("variacion_porcentual") val variacionPorcentual: Double?
)
