package ar.um.econsumo.data.repository

import android.util.Log
import ar.um.econsumo.data.ApiService
import ar.um.econsumo.data.NicsResponse
import ar.um.econsumo.data.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.await

/**
 * Repositorio para manejar las operaciones relacionadas con los NICs
 */
class NicRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager? = null
) {

    companion object {
        private const val TAG = "NicRepository"
    }

    /**
     * Obtiene todos los NICs disponibles del usuario autenticado
     * @return Lista de NICs como Strings
     * @deprecated Use getNicsConDetalle() instead to get detailed information
     */
    @Deprecated("Use getNicsConDetalle() instead to get detailed information")
    suspend fun getTodosLosNics(): List<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Obteniendo NICs con JWT...")
            val response = apiService.getNicsConJwt().execute()

            if (response.isSuccessful) {
                val nicsResponse = response.body()
                if (nicsResponse != null) {
                    Log.d(TAG, "NICs obtenidos con éxito: ${nicsResponse.nics.size}")
                    return@withContext nicsResponse.nics
                } else {
                    Log.w(TAG, "Respuesta vacía del servidor")
                    return@withContext emptyList()
                }
            } else {
                Log.e(TAG, "Error al cargar NICs con JWT: ${response.code()} - ${response.message()}")
                throw Exception("Error al cargar NICs: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener NICs con JWT", e)
            throw e
        }
    }

    /**
     * Obtiene NICs con información detallada del usuario autenticado
     * @return Objeto NicsResponse con información completa de los NICs
     */
    suspend fun getNicsConDetalle(): NicsResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Obteniendo NICs detallados con JWT...")
            val response = apiService.getNicsConJwt().execute()

            if (response.isSuccessful) {
                val nicsResponse = response.body()
                if (nicsResponse != null) {
                    Log.d(TAG, "NICs detallados obtenidos con éxito: ${nicsResponse.nics.size} NICs para usuario ${nicsResponse.usuario}")
                    return@withContext nicsResponse
                } else {
                    Log.w(TAG, "Respuesta vacía del servidor")
                    throw Exception("El servidor devolvió una respuesta vacía")
                }
            } else {
                Log.e(TAG, "Error al cargar NICs detallados con JWT: ${response.code()} - ${response.message()}")
                throw Exception("Error al cargar NICs: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener NICs detallados con JWT", e)
            throw e
        }
    }
}
