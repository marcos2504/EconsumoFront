package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

/**
 * Modelo para recibir la respuesta del endpoint que devuelve los NICs disponibles
 */
data class NicsResponse(
    @SerializedName("nics") val nics: List<String> = emptyList()
)
