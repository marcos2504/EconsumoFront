package ar.um.econsumo.data.repository

import android.util.Log
import ar.um.econsumo.data.ApiService
import ar.um.econsumo.data.SyncResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Repositorio para manejar la sincronización de facturas desde Gmail
 */
class SyncRepository(private val apiService: ApiService) {

    private val TAG = "SyncRepository"

    /**
     * Sincroniza las facturas con el número dado de emails
     * @param maxEmails El número máximo de emails a procesar
     * @return Respuesta del servidor con información sobre las facturas sincronizadas
     */
    suspend fun syncFacturas(maxEmails: Int): Result<SyncResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando sincronización con $maxEmails emails")
            val response = apiService.syncFacturas(maxEmails).execute()

            if (response.isSuccessful && response.body() != null) {
                val syncResponse = response.body()!!
                Log.d(TAG, "Sincronización exitosa: ${syncResponse.emailsProcesados} emails procesados, ${syncResponse.facturasEncontradas} facturas encontradas")
                Result.success(syncResponse)
            } else {
                val errorMessage = "Error en la sincronización: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción durante la sincronización", e)
            Result.failure(e)
        }
    }
}
