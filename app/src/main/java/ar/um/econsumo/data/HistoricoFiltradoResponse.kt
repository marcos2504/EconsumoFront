package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

data class HistoricoFiltradoResponse(
    val nic: String,
    val usuario: String,
    @SerializedName("filtros_aplicados")
    val filtrosAplicados: FiltrosAplicados,
    @SerializedName("total_registros")
    val totalRegistros: Int,
    val datos: List<DatoConsumo>,
    val estadisticas: Estadisticas
)

data class FiltrosAplicados(
    @SerializedName("fecha_desde")
    val fechaDesde: String?,
    @SerializedName("fecha_hasta")
    val fechaHasta: String?,
    @SerializedName("ultimos_meses")
    val ultimosMeses: Int?,
    @SerializedName("ordenar_por")
    val ordenarPor: String?,
    val orden: String?
)
