package ar.um.econsumo.data.repository

import android.util.Log
import ar.um.econsumo.data.ApiService
import ar.um.econsumo.data.EstadoSyncResponse
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
     * Verifica el estado de sincronización del usuario
     * @return Respuesta con el estado de sincronización (si necesita o no hacer sync inicial)
     */
    suspend fun verificarEstadoSync(): Result<EstadoSyncResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Verificando estado de sincronización")
            val response = apiService.getEstadoSync().execute()

            if (response.isSuccessful && response.body() != null) {
                val estadoSync = response.body()!!
                Log.d(TAG, "Estado sync: tiene_facturas=${estadoSync.tieneFacturas}, necesita_sync_inicial=${estadoSync.necesitaSyncInicial}")
                Result.success(estadoSync)
            } else {
                val errorMessage = "Error al verificar estado sync: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al verificar estado sync", e)
            Result.failure(e)
        }
    }

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

    /**
     * Sincroniza las facturas de forma inteligente usando JWT
     * @param maxEmails El número máximo de emails a procesar
     * @param forzarSync Si se debe forzar la sincronización aunque ya haya suficientes facturas
     * @return Respuesta del servidor con información sobre las facturas sincronizadas
     */
    suspend fun syncInteligente(maxEmails: Int, forzarSync: Boolean = false): Result<SyncResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando sincronización inteligente con $maxEmails emails, forzarSync=$forzarSync")
            val response = apiService.syncInteligente(maxEmails, forzarSync).execute()

            if (response.isSuccessful && response.body() != null) {
                val syncResponse = response.body()!!
                Log.d(TAG, "Sincronización inteligente exitosa: ${syncResponse.emailsProcesados} emails procesados, ${syncResponse.facturasEncontradas} facturas encontradas")
                Result.success(syncResponse)
            } else {
                val errorMessage = "Error en la sincronización inteligente: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción durante la sincronización inteligente", e)
            Result.failure(e)
        }
    }
}
