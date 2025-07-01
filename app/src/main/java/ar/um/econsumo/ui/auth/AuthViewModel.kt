package ar.um.econsumo.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.um.econsumo.data.AuthResponse
import ar.um.econsumo.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val token: String) : AuthState()
    object OAuthCompleted : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    fun androidOAuth(email: String, idToken: String, serverAuthCode: String?) {
        _authState.value = AuthState.Loading

        Log.d("AuthViewModel", "Iniciando autenticación con Google: Email=$email, IdToken=${idToken.take(10)}..., ServerAuthCode=$serverAuthCode")

        viewModelScope.launch {
            try {
                // IMPORTANTE: Usar siempre el idToken, NO el serverAuthCode
                // El servidor espera específicamente un ID token de Google
                Log.d("AuthViewModel", "Usando ID Token para autenticación, longitud: ${idToken.length}")

                Log.d("AuthViewModel", "Llamando a authenticateWithGoogle en el repositorio...")
                val call = authRepository.authenticateWithGoogle(email, idToken, serverAuthCode)

                // No acceder a call.request().url aquí para evitar errores

                call.enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        // Aquí sí podemos acceder a la URL de forma segura
                        Log.d("AuthViewModel", "URL de la petición: ${call.request().url}")
                        Log.d("AuthViewModel", "Respuesta recibida: código=${response.code()}, mensaje=${response.message()}")

                        if (response.isSuccessful && response.body() != null) {
                            val authResponse = response.body()!!
                            Log.d("AuthViewModel", "Autenticación exitosa: ${authResponse.token.take(10)}...")
                            authRepository.saveAuthToken(authResponse.token)
                            _authState.value = AuthState.Authenticated(authResponse.token)
                        } else {
                            val errorMessage = "Error en autenticación: ${response.code()} - ${response.message()}"
                            Log.e("AuthViewModel", errorMessage)

                            // Intentar leer el cuerpo del error para más detalles
                            try {
                                val errorBody = response.errorBody()?.string()
                                Log.e("AuthViewModel", "Cuerpo del error: $errorBody")
                                // Usar el errorBody como parte del mensaje de error si está disponible
                                _authState.value = AuthState.Error(
                                    if (!errorBody.isNullOrBlank()) "$errorMessage. Detalle: $errorBody"
                                    else errorMessage
                                )
                            } catch (e: Exception) {
                                Log.e("AuthViewModel", "No se pudo leer el cuerpo del error", e)
                                _authState.value = AuthState.Error(errorMessage)
                            }
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        // Aquí también podemos acceder a la URL de forma segura
                        try {
                            Log.d("AuthViewModel", "URL de la petición fallida: ${call.request().url}")
                        } catch (e: Exception) {
                            Log.e("AuthViewModel", "No se pudo acceder a la URL de la petición", e)
                        }

                        val errorMessage = "Error en la comunicación con el servidor: ${t.message}"
                        Log.e("AuthViewModel", errorMessage, t)
                        _authState.value = AuthState.Error(errorMessage)
                    }
                })
            } catch (e: Exception) {
                val errorMessage = "Error inesperado: ${e.message}"
                Log.e("AuthViewModel", errorMessage, e)
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun reset() {
        _authState.value = AuthState.Initial
    }
}

