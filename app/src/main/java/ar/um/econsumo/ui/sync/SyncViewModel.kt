package ar.um.econsumo.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.um.econsumo.data.SyncResponse
import ar.um.econsumo.data.repository.AuthRepository
import ar.um.econsumo.data.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Estados de la sincronización
 */
sealed class SyncState {
    object Initial : SyncState()
    object Loading : SyncState()
    data class Success(val response: SyncResponse) : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * ViewModel para la pantalla de sincronización
 */
class SyncViewModel(
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Initial)
    val syncState: StateFlow<SyncState> = _syncState

    /**
     * Sincroniza las facturas con el número dado de emails
     */
    fun syncFacturas(maxEmails: Int) {
        _syncState.value = SyncState.Loading

        viewModelScope.launch {
            syncRepository.syncFacturas(maxEmails)
                .onSuccess { response ->
                    _syncState.value = SyncState.Success(response)
                }
                .onFailure { error ->
                    _syncState.value = SyncState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    /**
     * Verifica si el usuario está autenticado
     */
    fun isLoggedIn(): Boolean {
        return authRepository.isAuthenticated()
    }

    /**
     * Obtiene el nombre de usuario
     */
    fun getUserName(): String {
        return authRepository.getUserName() ?: "Usuario"
    }

    /**
     * Resetea el estado del ViewModel
     */
    fun resetState() {
        _syncState.value = SyncState.Initial
    }
}
