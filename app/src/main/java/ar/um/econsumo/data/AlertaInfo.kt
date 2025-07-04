package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

data class AlertaInfo(
    val nic: String? = null,
    val fecha: String? = null,
    @SerializedName("consumo_kwh") val consumoKwh: Double? = null,
    val anomalia: Boolean? = null,
    val estado: String? = null,
    @SerializedName("comparado_trimestre") val comparadoTrimestre: Double? = null,
    @SerializedName("score_anomalia") val scoreAnomalia: Double? = null,
    val mensaje: String? = null
)
