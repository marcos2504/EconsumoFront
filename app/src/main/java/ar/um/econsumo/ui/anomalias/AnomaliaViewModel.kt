package ar.um.econsumo.ui.anomalias

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.um.econsumo.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "AnomaliaViewModel"

sealed class AnomaliaUIState {
    object Loading : AnomaliaUIState()
    data class Success(val data: ConsumoResponse) : AnomaliaUIState()
    data class Error(val message: String) : AnomaliaUIState()
}

sealed class UltimoConsumoUIState {
    object Loading : UltimoConsumoUIState()
    data class Success(val data: UltimoConsumoResponse) : UltimoConsumoUIState()
    data class Error(val message: String) : UltimoConsumoUIState()
}

sealed class AnomaliasListUIState {
    object Loading : AnomaliasListUIState()
    data class Success(val data: AnomaliasJwtResponse) : AnomaliasListUIState()
    data class Error(val message: String) : AnomaliasListUIState()
}

class AnomaliaViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<AnomaliaUIState>(AnomaliaUIState.Loading)
    val uiState: StateFlow<AnomaliaUIState> = _uiState

    private val _ultimoConsumoState = MutableStateFlow<UltimoConsumoUIState>(UltimoConsumoUIState.Loading)
    val ultimoConsumoState: StateFlow<UltimoConsumoUIState> = _ultimoConsumoState

    private val _listUiState = MutableStateFlow<AnomaliasListUIState>(AnomaliasListUIState.Loading)
    val listUiState: StateFlow<AnomaliasListUIState> = _listUiState

    fun consultarConsumo(nic: String) {
        _uiState.value = AnomaliaUIState.Loading

        apiService.consultarConsumo(nic).enqueue(object : Callback<ConsumoResponse> {
            override fun onResponse(call: Call<ConsumoResponse>, response: Response<ConsumoResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { consumoResponse ->
                        Log.d(TAG, "Consumo consultado con éxito: ${consumoResponse.nic}")
                        _uiState.value = AnomaliaUIState.Success(consumoResponse)
                    } ?: run {
                        _uiState.value = AnomaliaUIState.Error("Respuesta vacía del servidor")
                    }
                } else {
                    _uiState.value = AnomaliaUIState.Error("Error ${response.code()}: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ConsumoResponse>, t: Throwable) {
                Log.e(TAG, "Error al consultar consumo", t)
                _uiState.value = AnomaliaUIState.Error("Error de conexión: ${t.message}")
            }
        })
    }

    fun consultarUltimoConsumo(nic: String) {
        _ultimoConsumoState.value = UltimoConsumoUIState.Loading

        apiService.consultarUltimoConsumo(nic).enqueue(object : Callback<UltimoConsumoResponse> {
            override fun onResponse(call: Call<UltimoConsumoResponse>, response: Response<UltimoConsumoResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { consumoResponse ->
                        Log.d(TAG, "Último consumo consultado con éxito: ${consumoResponse.nic}")
                        _ultimoConsumoState.value = UltimoConsumoUIState.Success(consumoResponse)
                    } ?: run {
                        _ultimoConsumoState.value = UltimoConsumoUIState.Error("Respuesta vacía del servidor")
                    }
                } else {
                    _ultimoConsumoState.value = UltimoConsumoUIState.Error("Error ${response.code()}: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UltimoConsumoResponse>, t: Throwable) {
                Log.e(TAG, "Error al consultar último consumo", t)
                _ultimoConsumoState.value = UltimoConsumoUIState.Error("Error de conexión: ${t.message}")
            }
        })
    }

    fun consultarUltimoConsumoConJwt(nic: String) {
        _ultimoConsumoState.value = UltimoConsumoUIState.Loading

        apiService.consultarUltimoConsumoConJwt(nic).enqueue(object : Callback<UltimoConsumoResponse> {
            override fun onResponse(call: Call<UltimoConsumoResponse>, response: Response<UltimoConsumoResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { consumoResponse ->
                        Log.d(TAG, "Último consumo consultado con JWT: ${consumoResponse.nic}")
                        _ultimoConsumoState.value = UltimoConsumoUIState.Success(consumoResponse)
                    } ?: run {
                        _ultimoConsumoState.value = UltimoConsumoUIState.Error("Respuesta vacía del servidor")
                    }
                } else {
                    _ultimoConsumoState.value = UltimoConsumoUIState.Error("Error ${response.code()}: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UltimoConsumoResponse>, t: Throwable) {
                Log.e(TAG, "Error al consultar último consumo con JWT", t)
                _ultimoConsumoState.value = UltimoConsumoUIState.Error("Error de conexión: ${t.message}")
            }
        })
    }

    fun obtenerTodasLasAnomalias(nic: String, useJwt: Boolean = false) {
        _listUiState.value = AnomaliasListUIState.Loading

        val call = if (useJwt) {
            apiService.verTodasAnomaliasConJwt(nic)
        } else {
            apiService.verTodasAnomalias(nic)
        }

        call.enqueue(object : Callback<AnomaliasJwtResponse> {
            override fun onResponse(call: Call<AnomaliasJwtResponse>, response: Response<AnomaliasJwtResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { anomaliasResponse ->
                        Log.d(TAG, "Anomalías obtenidas con éxito:")
                        _listUiState.value = AnomaliasListUIState.Success(anomaliasResponse)
                    } ?: run {
                        _listUiState.value = AnomaliasListUIState.Error("Respuesta vacía del servidor")
                    }
                } else {
                    _listUiState.value = AnomaliasListUIState.Error("Error ${response.code()}: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<AnomaliasJwtResponse>, t: Throwable) {
                Log.e(TAG, "Error al obtener anomalías", t)
                _listUiState.value = AnomaliasListUIState.Error("Error de conexión: ${t.message}")
            }
        })
    }
}
