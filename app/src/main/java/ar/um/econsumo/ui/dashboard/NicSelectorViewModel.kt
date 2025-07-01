package ar.um.econsumo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.um.econsumo.data.repository.NicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Estados posibles para la pantalla de selección de NIC
 */
sealed class NicSelectorState {
    object Loading : NicSelectorState()
    // Estado de no autenticado eliminado, ya que nunca debería mostrarse en esta pantalla
    data class Success(val nics: List<String>) : NicSelectorState()
    data class Error(val message: String) : NicSelectorState()
}

/**
 * ViewModel para manejar la lógica de la pantalla de selección de NIC
 */
class NicSelectorViewModel(private val nicRepository: NicRepository) : ViewModel() {

    // Estado de la pantalla
    private val _state = MutableStateFlow<NicSelectorState>(NicSelectorState.Loading) // Iniciar en Loading
    val state: StateFlow<NicSelectorState> = _state

    // NIC seleccionado actualmente
    private val _selectedNic = MutableStateFlow<String?>(null)
    val selectedNic: StateFlow<String?> = _selectedNic

    // Iniciar cargando NICs automáticamente
    init {
        loadNics()
    }

    /**
     * Carga la lista de NICs desde el repositorio
     * Si el usuario está autenticado, cargará los NICs correctamente
     */
    fun loadNics() {
        _state.value = NicSelectorState.Loading

        viewModelScope.launch {
            try {
                val nics = nicRepository.getTodosLosNics()
                _state.value = NicSelectorState.Success(nics)

                // Seleccionar el primer NIC por defecto si hay alguno disponible
                if (nics.isNotEmpty() && _selectedNic.value == null) {
                    _selectedNic.value = nics[0]
                }
            } catch (e: Exception) {
                _state.value = NicSelectorState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Actualiza el NIC seleccionado
     */
    fun selectNic(nic: String) {
        _selectedNic.value = nic
    }

    /**
     * Verifica si se ha seleccionado un NIC válido
     */
    fun isNicSelected(): Boolean {
        return !_selectedNic.value.isNullOrBlank()
    }

    /**
     * Obtiene el NIC seleccionado o una cadena vacía si no hay ninguno
     */
    fun getSelectedNic(): String {
        return _selectedNic.value ?: ""
    }
}
