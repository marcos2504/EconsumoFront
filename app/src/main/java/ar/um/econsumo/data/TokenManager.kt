package ar.um.econsumo.data

import android.content.Context
import android.util.Log

/**
 * Clase para gestionar el token JWT
 * Usa las mismas preferencias y claves que AuthRepository para garantizar la coherencia
 */
class TokenManager(context: Context) {
    // Usar exactamente las mismas constantes que AuthRepository
    companion object {
        private const val PREFS_NAME = "ar.um.econsumo.auth"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val TAG = "TokenManager"
    }

    // Usar las mismas preferencias compartidas que AuthRepository
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        Log.d(TAG, "Guardando token JWT (${token.length} caracteres)")
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getToken(): String? {
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        Log.d(TAG, "Obteniendo token JWT: ${if (token != null) "disponible (${token.length} caracteres)" else "no disponible"}")
        return token
    }

    fun clearToken() {
        Log.d(TAG, "Eliminando token JWT")
        prefs.edit().remove(KEY_AUTH_TOKEN).apply()
    }
}

