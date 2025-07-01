package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

data class SyncResponse(
    @SerializedName("emails_procesados") val emailsProcesados: Int,
    @SerializedName("facturas_encontradas") val facturasEncontradas: Int,
    @SerializedName("facturas_sincronizadas") val facturasSincronizadas: Int,
    @SerializedName("tiempo_transcurrido") val tiempoTranscurrido: String,
    @SerializedName("facturas") val facturas: List<Factura>
)
