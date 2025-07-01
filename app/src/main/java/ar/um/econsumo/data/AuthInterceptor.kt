package ar.um.econsumo.data

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Interceptor JWT para añadir automáticamente el token a las peticiones HTTP.
 * Formatea el token exactamente como lo espera el backend FastAPI:
 * "Authorization: Bearer <token>"
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        // Log para debugging
        Log.d(TAG, "Interceptando petición a: $url")

        val token = tokenManager.getToken()
        Log.d(TAG, "Token disponible: ${!token.isNullOrEmpty()}, longitud: ${token?.length ?: 0}")

        // Añadir el token JWT a las peticiones
        val newRequest = if (!token.isNullOrEmpty()) {
            // Formato exacto: "Bearer " + token (con espacio después de Bearer)
            // FastAPI con HTTPBearer espera este formato específico
            val authHeader = "Bearer $token"
            Log.d(TAG, "Header de autorización: Bearer ${token.take(15)}...")

            val updatedRequest = request.newBuilder()
                .header("Authorization", authHeader)
                .build()

            // Imprimir los headers para verificación
            Log.d(TAG, "Headers de la petición:")
            updatedRequest.headers.forEach { header ->
                Log.d(TAG, "  ${header.first}: ${
                    if (header.first == "Authorization")
                        "Bearer ${header.second.substring(7).take(15)}..."
                    else header.second
                }")
            }

            updatedRequest
        } else {
            Log.w(TAG, "No hay token disponible, enviando petición sin autenticación")
            request
        }

        // Procede con la petición
        val response = chain.proceed(newRequest)
        Log.d(TAG, "Respuesta recibida: ${response.code} para URL: $url")

        // Si recibimos un 401 o 403, es un problema de autenticación
        if (response.code == 401 || response.code == 403) {
            Log.w(TAG, "Error de autenticación: ${response.code} al acceder a: $url")
            // Imprimir el cuerpo del error si está disponible
            try {
                val responseBodyCopy = response.peekBody(Long.MAX_VALUE)
                val errorBody = responseBodyCopy.string()
                Log.w(TAG, "Respuesta de error: $errorBody")
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo leer el cuerpo de la respuesta: ${e.message}")
            }
        }

        return response
    }
}
