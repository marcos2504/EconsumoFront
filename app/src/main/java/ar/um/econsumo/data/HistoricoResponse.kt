package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

data class HistoricoResponse(
    val nic: String,
    val usuario: String,
    val direccion: String,
    @SerializedName("total_registros")
    val totalRegistros: Int,
    val datos: List<DatoConsumo>,
    @SerializedName("para_grafico")
    val paraGrafico: List<DatoGrafico>,
    val estadisticas: Estadisticas
)

data class DatoConsumo(
    val id: Int,
    val fecha: String,
    @SerializedName("consumo_kwh")
    val consumoKwh: Float,
    @SerializedName("factura_id")
    val facturaId: Int
)

data class DatoGrafico(
    val fecha: String,
    val consumo: Float
)

data class Estadisticas(
    val promedio: Float,
    val maximo: Float,
    val minimo: Float,
    val tendencia: String?,
    @SerializedName("cambio_porcentual")
    val cambioPorcentual: Float?,
    @SerializedName("total_meses")
    val totalMeses: Int,
    @SerializedName("total_consumo")
    val totalConsumo: Float? = null,
    @SerializedName("periodo_meses")
    val periodoMeses: Int? = null
)
