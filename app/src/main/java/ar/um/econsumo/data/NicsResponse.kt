package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

/**
 * Clase que representa la respuesta del endpoint /facturas/nics_con_jwt
 */
data class NicsResponse(
    @SerializedName("nics") val nics: List<String> = emptyList(),
    @SerializedName("nics_con_direccion") val nicsConDireccion: List<NicConDireccion> = emptyList(),
    @SerializedName("selector_items") val selectorItems: List<SelectorItem> = emptyList(),
    @SerializedName("total_nics") val totalNics: Int = 0,
    @SerializedName("usuario") val usuario: String? = null
)

/**
 * Información detallada de un NIC con su dirección
 */
data class NicConDireccion(
    @SerializedName("nic") val nic: String,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("direccion_corta") val direccionCorta: String,
    @SerializedName("ultima_fecha") val ultimaFecha: String,
    @SerializedName("total_facturas") val totalFacturas: Int
)

/**
 * Item formateado para mostrar en el selector de NICs
 */
data class SelectorItem(
    @SerializedName("value") val value: String,
    @SerializedName("label") val label: String,
    @SerializedName("label_completo") val labelCompleto: String,
    @SerializedName("info") val info: String,
    @SerializedName("subtitle") val subtitle: String
)
