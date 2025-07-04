package ar.um.econsumo.data

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del endpoint para verificar el estado de sincronización del usuario
 */
data class EstadoSyncResponse(
    @SerializedName("usuario") val usuario: String,
    @SerializedName("tiene_facturas") val tieneFacturas: Boolean,
    @SerializedName("total_facturas") val totalFacturas: Int,
    @SerializedName("primera_sync_completada") val primeraSyncCompletada: Boolean,
    @SerializedName("ultima_factura") val ultimaFactura: UltimaFactura?,
    @SerializedName("necesita_sync_inicial") val necesitaSyncInicial: Boolean
)

data class UltimaFactura(
    @SerializedName("fecha") val fecha: String,
    @SerializedName("nic") val nic: String
)
