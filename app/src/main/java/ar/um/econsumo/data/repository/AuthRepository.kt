package ar.um.econsumo.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ar.um.econsumo.data.ApiService
import ar.um.econsumo.data.AuthResponse
import retrofit2.Call

class AuthRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "ar.um.econsumo.auth"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val TAG = "AuthRepository"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun authenticateWithGoogle(email: String, token: String, serverAuthCode: String? = null): Call<AuthResponse> {
        // Log para debugging detallado
        Log.d(TAG, "Enviando autenticación al backend:")
        Log.d(TAG, "  - Email: $email")
        Log.d(TAG, "  - Token (idToken) length: ${token.length}")
        Log.d(TAG, "  - ServerAuthCode disponible: ${serverAuthCode != null}")
        if (serverAuthCode != null) {
            Log.d(TAG, "  - ServerAuthCode length: ${serverAuthCode.length}")
        }

        // IMPORTANTE: Usar idToken y serverAuthCode según el nuevo endpoint
        Log.d(TAG, "Usando el endpoint /auth/android para autenticación")

        // Usar el nuevo método que envía los parámetros al endpoint /auth/android
        return apiService.authenticateWithAndroid(email, token, serverAuthCode)
    }

    fun saveAuthToken(token: String) {
        Log.d(TAG, "Guardando token de autenticación (${token.length} caracteres)")
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        Log.d(TAG, "Obteniendo token guardado: ${token != null}")
        return token
    }

    fun clearAuthToken() {
        Log.d(TAG, "Borrando token de autenticación")
        prefs.edit().remove(KEY_AUTH_TOKEN).apply()
    }

    fun isAuthenticated(): Boolean {
        return getAuthToken() != null
    }

    /**
     * Obtiene el nombre de usuario
     * Nota: Actualmente devuelve un valor por defecto, ya que no estamos guardando esta información
     */
    fun getUserName(): String? {
        // Para una implementación completa, aquí se recuperaría el nombre del usuario
        // desde las SharedPreferences o desde el token JWT decodificado
        return "Usuario"
    }
}
