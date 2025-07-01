package ar.um.econsumo.data

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.100.12:8000/" // localhost para emulador
    private const val TAG = "RetrofitClient"

    // Almacena el contexto de la aplicación para crear TokenManager cuando sea necesario
    var appContext: Context? = null

    // TokenManager se inicializa con getter seguro
    private val tokenManager: TokenManager by lazy {
        // Si el contexto no se ha establecido, crear una instancia mínima sin interceptor JWT
        appContext?.let { TokenManager(it) } ?: throw IllegalStateException("RetrofitClient no ha sido inicializado. Llama a RetrofitClient.init(context) antes de usar.")
    }

    // Inicializar con el contexto para poder crear el TokenManager
    fun init(context: Context) {
        appContext = context.applicationContext // Usar applicationContext para evitar memory leaks
        Log.d(TAG, "RetrofitClient inicializado con el contexto: ${context.applicationContext}")
    }

    // Configurar HttpLoggingInterceptor para ver detalles completos de las peticiones
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttpClient con el interceptor JWT y el interceptor de logging
    private val okHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addInterceptor(loggingInterceptor) // Añadir primero el logging interceptor
            if (appContext != null) {
                Log.d(TAG, "Añadiendo AuthInterceptor a OkHttpClient")
                addInterceptor(AuthInterceptor(tokenManager))
            } else {
                Log.e(TAG, "No se puede añadir AuthInterceptor: appContext es null")
            }
        }.build()
    }

    val instance: ApiService by lazy {
        Log.d(TAG, "Creando instancia de ApiService")
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}

