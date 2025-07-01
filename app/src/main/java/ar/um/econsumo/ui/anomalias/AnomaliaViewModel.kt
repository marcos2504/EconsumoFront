package ar.um.econsumo.ui.anomalias

import android.content.Context
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

sealed class AnomaliasListUIState {
    object Loading : AnomaliasListUIState()
    data class Success(val data: AnomaliasJwtResponse) : AnomaliasListUIState()
    data class Error(val message: String) : AnomaliasListUIState()
}

class AnomaliaViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<AnomaliaUIState>(AnomaliaUIState.Loading)
    val uiState: StateFlow<AnomaliaUIState> = _uiState

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

    fun obtenerTodasLasAnomalias(nic: String) {
        _listUiState.value = AnomaliasListUIState.Loading

        apiService.getAnomaliasConJwt(nic).enqueue(object : Callback<AnomaliasJwtResponse> {
            override fun onResponse(call: Call<AnomaliasJwtResponse>, response: Response<AnomaliasJwtResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { anomaliasResponse ->
                        Log.d(TAG, "Anomalías obtenidas con éxito: ${anomaliasResponse.anomalias.size}")
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
