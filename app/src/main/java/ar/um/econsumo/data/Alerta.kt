package ar.um.econsumo.data

data class Alerta(
    val fecha: String,
    val consumo_kwh: Double,
    val anomalia: Boolean,
    val score: Double,
    val comparado_trimestre: Double? = null
)