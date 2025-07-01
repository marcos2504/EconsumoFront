package ar.um.econsumo.di

import android.content.Context
import ar.um.econsumo.data.ApiService
import ar.um.econsumo.data.RetrofitClient
import ar.um.econsumo.data.TokenManager
import ar.um.econsumo.data.repository.AuthRepository
import ar.um.econsumo.data.repository.NicRepository
import ar.um.econsumo.data.repository.SyncRepository
import ar.um.econsumo.ui.anomalias.AnomaliaViewModel
import ar.um.econsumo.ui.auth.AuthViewModel
import ar.um.econsumo.ui.dashboard.NicSelectorViewModel
import ar.um.econsumo.ui.sync.SyncViewModel

/**
 * Clase que maneja las dependencias de la aplicación
 * Implementa un patrón de Service Locator simplificado
 */
object AppDependencies {
    // Gestor de token JWT
    private fun getTokenManager(context: Context): TokenManager {
        return TokenManager(context)
    }

    // API Services - Ahora usa RetrofitClient que tiene el interceptor JWT configurado
    private fun getApiService(context: Context): ApiService {
        // Asegurarse de que RetrofitClient esté inicializado
        RetrofitClient.init(context)
        return RetrofitClient.instance
    }

    // Repositorios
    private fun getAuthRepository(context: Context): AuthRepository {
        return AuthRepository(getApiService(context), context)
    }

    private fun getSyncRepository(context: Context): SyncRepository {
        return SyncRepository(getApiService(context))
    }

    private fun getNicRepository(context: Context): NicRepository {
        return NicRepository(getApiService(context), getTokenManager(context))
    }

    // ViewModels
    fun getAuthViewModel(context: Context): AuthViewModel {
        return AuthViewModel(getAuthRepository(context))
    }

    fun getSyncViewModel(context: Context): SyncViewModel {
        return SyncViewModel(getSyncRepository(context), getAuthRepository(context))
    }

    fun getNicSelectorViewModel(context: Context): NicSelectorViewModel {
        return NicSelectorViewModel(getNicRepository(context))
    }

    // ViewModel para anomalías
    fun getAnomaliaViewModel(context: Context = RetrofitClient.appContext!!): AnomaliaViewModel {
        return AnomaliaViewModel(getApiService(context))
    }
}
