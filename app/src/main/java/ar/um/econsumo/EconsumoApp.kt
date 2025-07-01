package ar.um.econsumo

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import ar.um.econsumo.data.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Clase de aplicación principal para Econsumo.
 * Esta clase se inicia antes que cualquier actividad y es un buen lugar para
 * inicializar componentes que necesitan estar disponibles durante toda la vida de la app.
 */
class EconsumoApp : Application() {

    companion object {
        private const val TAG = "EconsumoApp"
    }

    // Ámbito de corrutina para operaciones en segundo plano
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Preferencias compartidas para almacenar tokens y configuración
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Aplicación iniciada correctamente")

        // Inicializar preferencias compartidas
        sharedPreferences = getSharedPreferences("econsumo_prefs", MODE_PRIVATE)

        // Inicializar RetrofitClient con el contexto de la aplicación
        RetrofitClient.init(applicationContext)
        Log.d(TAG, "RetrofitClient inicializado correctamente")

        // Verificar conectividad con el backend
        checkBackendConnectivity()
    }

    /**
     * Verifica la conectividad con el backend y cambia a la URL de respaldo si es necesario
     */
    private fun checkBackendConnectivity() {
        applicationScope.launch {
            try {
                // Intenta realizar una llamada simple al backend para verificar la conectividad
                val response = RetrofitClient.instance.testConnection().execute()

                if (response.isSuccessful) {
                    Log.d(TAG, "Conexión exitosa con el servidor principal")
                } else {
                    Log.w(TAG, "Error en la conexión con el servidor principal: ${response.code()} - ${response.message()}")

                    if (response.code() == 502) {
                        Log.w(TAG, "Error 502 Bad Gateway - El servidor puede estar caído o mal configurado")
                        handleBackendError("El servidor está temporalmente no disponible (Error 502)")

                        // Intenta usar la URL de respaldo
                    }
                }
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Timeout al conectar con el servidor", e)
                handleBackendError("Tiempo de espera agotado al intentar conectar con el servidor")
            } catch (e: UnknownHostException) {
                Log.e(TAG, "No se puede resolver el host del servidor", e)
                handleBackendError("No se puede encontrar el servidor. Verifica tu conexión a Internet")

            } catch (e: IOException) {
                Log.e(TAG, "Error de I/O al conectar con el servidor", e)
                handleBackendError("Error de conexión: ${e.message}")

            } catch (e: Exception) {
                Log.e(TAG, "Error desconocido al conectar con el servidor", e)
                handleBackendError("Error inesperado: ${e.message}")
            }
        }
    }

    /**
     * Maneja un error de backend mostrando un Toast en el hilo principal
     */
    private fun handleBackendError(message: String) {
        val mainHandler = android.os.Handler(mainLooper)
        mainHandler.post {
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
