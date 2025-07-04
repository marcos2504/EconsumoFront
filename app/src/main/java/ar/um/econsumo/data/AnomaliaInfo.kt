package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

data class AnomaliaInfo(
    val id: Int? = null,
    val fecha: String? = null,
    @SerializedName("consumo_kwh") val consumoKwh: Double? = null,
    @SerializedName("es_anomalia") val esAnomalia: Boolean? = null,
    @SerializedName("variacion_trimestre") val variacionTrimestre: Double? = null,
    @SerializedName("score_anomalia") val scoreAnomalia: Double? = null,
    val trimestre: Int? = null,
    val año: Int? = null,
    @SerializedName("tipo_anomalia") val tipoAnomalia: String? = null,
    val icono: String? = null,
    @SerializedName("factura_id") val facturaId: Int? = null,
    val mensaje: String? = null
)
