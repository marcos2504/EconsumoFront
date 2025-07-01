package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

data class ResumenRapidoResponse(
    val nic: String,
    val estado: String,
    @SerializedName("ultimo_consumo")
    val ultimoConsumo: Float,
    @SerializedName("fecha_ultimo")
    val fechaUltimo: String,
    @SerializedName("promedio_reciente")
    val promedioReciente: Float,
    @SerializedName("total_meses")
    val totalMeses: Int,
    @SerializedName("datos_recientes")
    val datosRecientes: List<DatoGrafico>
)
