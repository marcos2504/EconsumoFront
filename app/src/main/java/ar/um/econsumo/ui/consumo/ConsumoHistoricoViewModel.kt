package ar.um.econsumo.ui.consumo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.um.econsumo.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ConsumoHistoricoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ConsumoUiState>(ConsumoUiState.Loading)
    val uiState: StateFlow<ConsumoUiState> = _uiState.asStateFlow()

    private val _filtros = MutableStateFlow(FiltrosHistorico())
    val filtros: StateFlow<FiltrosHistorico> = _filtros.asStateFlow()

    // Corregir la referencia al apiService
    private val apiService = RetrofitClient.instance

    fun cargarHistorico(nic: String) {
        _uiState.value = ConsumoUiState.Loading
        apiService.getHistoricoConsumo(nic).enqueue(object : Callback<HistoricoResponse> {
            override fun onResponse(call: Call<HistoricoResponse>, response: Response<HistoricoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ConsumoUiState.Success(response.body()!!)
                } else {
                    _uiState.value = ConsumoUiState.Error("Error al cargar datos: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<HistoricoResponse>, t: Throwable) {
                _uiState.value = ConsumoUiState.Error("Fallo al conectar con el servidor: ${t.message}")
            }
        })
    }

    fun cargarResumenRapido(nic: String, meses: Int = 6) {
        _uiState.value = ConsumoUiState.Loading
        apiService.getResumenRapido(nic, meses).enqueue(object : Callback<ResumenRapidoResponse> {
            override fun onResponse(call: Call<ResumenRapidoResponse>, response: Response<ResumenRapidoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ConsumoUiState.ResumenRapido(response.body()!!)
                } else {
                    _uiState.value = ConsumoUiState.Error("Error al cargar resumen: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResumenRapidoResponse>, t: Throwable) {
                _uiState.value = ConsumoUiState.Error("Fallo al conectar con el servidor: ${t.message}")
            }
        })
    }

    fun cargarHistoricoFiltrado(nic: String) {
        _uiState.value = ConsumoUiState.Loading
        val filtrosActuales = _filtros.value

        apiService.getHistoricoFiltrado(
            nic = nic,
            fechaDesde = filtrosActuales.fechaDesde,
            fechaHasta = filtrosActuales.fechaHasta,
            ordenarPor = filtrosActuales.ordenarPor,
            orden = filtrosActuales.orden
        ).enqueue(object : Callback<HistoricoFiltradoResponse> {
            override fun onResponse(call: Call<HistoricoFiltradoResponse>, response: Response<HistoricoFiltradoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ConsumoUiState.Filtrado(response.body()!!)
                } else {
                    _uiState.value = ConsumoUiState.Error("Error al cargar datos filtrados: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<HistoricoFiltradoResponse>, t: Throwable) {
                _uiState.value = ConsumoUiState.Error("Fallo al conectar con el servidor: ${t.message}")
            }
        })
    }

    fun cargarHistoricoPorPeriodo(nic: String, periodo: String) {
        _uiState.value = ConsumoUiState.Loading
        apiService.getHistoricoPorPeriodo(nic, periodo).enqueue(object : Callback<HistoricoResponse> {
            override fun onResponse(call: Call<HistoricoResponse>, response: Response<HistoricoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ConsumoUiState.Success(response.body()!!)
                } else {
                    _uiState.value = ConsumoUiState.Error("Error al cargar datos del periodo: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<HistoricoResponse>, t: Throwable) {
                _uiState.value = ConsumoUiState.Error("Fallo al conectar con el servidor: ${t.message}")
            }
        })
    }

    fun actualizarFiltros(filtros: FiltrosHistorico) {
        viewModelScope.launch {
            _filtros.value = filtros
        }
    }
}

sealed class ConsumoUiState {
    object Loading : ConsumoUiState()
    data class Success(val data: HistoricoResponse) : ConsumoUiState()
    data class ResumenRapido(val data: ResumenRapidoResponse) : ConsumoUiState()
    data class Filtrado(val data: HistoricoFiltradoResponse) : ConsumoUiState()
    data class Error(val mensaje: String) : ConsumoUiState()
}

data class FiltrosHistorico(
    val fechaDesde: String? = null,
    val fechaHasta: String? = null,
    val ordenarPor: String = "fecha",
    val orden: String = "desc"
)
