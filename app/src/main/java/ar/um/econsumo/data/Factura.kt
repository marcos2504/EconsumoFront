package ar.um.econsumo.data

data class Factura(
    val id: Int,
    val nic: String,
    val fecha: String,
    val consumo_kwh: Double
)