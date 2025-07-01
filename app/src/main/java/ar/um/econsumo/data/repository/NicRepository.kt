package ar.um.econsumo.data.repository

import android.util.Log
import ar.um.econsumo.data.ApiService
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
     * Obtiene todos los NICs disponibles
     * @return Lista de NICs como Strings
     */
    suspend fun getTodosLosNics(): List<String> = withContext(Dispatchers.IO) {
        val response = apiService.getTodosLosNics().execute()

        if (response.isSuccessful) {
            return@withContext response.body() ?: emptyList()
        } else {
            throw Exception("Error al cargar NICs: ${response.code()} - ${response.message()}")
        }
    }
}
