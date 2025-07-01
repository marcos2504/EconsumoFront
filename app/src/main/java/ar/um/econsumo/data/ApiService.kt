package ar.um.econsumo.data

import ar.um.econsumo.data.Alerta
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("anomalias/alerta/{nic}")
    fun getAlerta(@Path("nic") nic: String): Call<Alerta>

    @GET("historico/nic/{nic}")
    fun getFacturas(@Path("nic") nic: String): Call<List<Factura>>

    // El token JWT se enviará automáticamente a través del interceptor
    @GET("facturas/nics")
    fun getTodosLosNics(): Call<List<String>>

    // Endpoints JWT para consulta de consumo y anomalías
    @GET("anomalias/consultar_consumo/{nic}")
    fun consultarConsumo(@Path("nic") nic: String): Call<ConsumoResponse>

    @GET("anomalias/alerta_con_jwt/{nic}")
    fun getAlertaConJwt(@Path("nic") nic: String): Call<AlertaJwtResponse>

    @GET("anomalias/anomalias_con_jwt/{nic}")
    fun getAnomaliasConJwt(@Path("nic") nic: String): Call<AnomaliasJwtResponse>

    // Método alternativo que envía el token explícitamente como un encabezado
    @GET("facturas/nics")
    fun getNicsWithExplicitAuth(@Header("Authorization") authHeader: String): Call<List<String>>

    // Método alternativo que envía el token como un parámetro de consulta (por si acaso)
    @GET("facturas/nics")
    fun getNicsWithToken(@Query("token") token: String): Call<List<String>>

    @POST("auth/token")
    fun authGoogle(): Call<AuthResponse>

    // Método principal - usando JSON (Body)
    @POST("auth/google")
    fun authenticateWithGoogleJson(
        @Body authRequest: GoogleAuthRequest
    ): Call<AuthResponse>

    // Método para usar query params en el endpoint principal
    @POST("auth/google")
    fun authenticateWithGoogleQuery(
        @Query("email") email: String,
        @Query("token") token: String
    ): Call<AuthResponse>

    // Método para usar query params en el endpoint de Android
    @POST("auth/android")
    fun authenticateWithAndroid(
        @Query("email") email: String,
        @Query("id_token") idToken: String,
        @Query("server_auth_code") serverAuthCode: String?
    ): Call<AuthResponse>

    // Método alternativo - usando parámetros de consulta (Query)
    @POST("auth/google/query")
    fun authenticateWithGoogle(
        @Query("email") email: String,
        @Query("token") token: String
    ): Call<AuthResponse>

    // Método alternativo - usando form-urlencoded
    @FormUrlEncoded
    @POST("auth/google/form")
    fun authenticateWithGoogleForm(
        @Field("email") email: String,
        @Field("token") token: String
    ): Call<AuthResponse>

    // Método para sincronizar facturas con parámetro
    @POST("facturas/sync")
    fun syncFacturas(@Query("max_emails") maxEmails: Int): Call<SyncResponse>

    /**
     * Endpoint simple para probar la conectividad con el servidor
     * Este método debe devolver un código 200 OK si el servidor está funcionando correctamente
     */
    @GET("auth/health")
    fun testConnection(): Call<Void>

    // Nuevos endpoints para consumo histórico
    @GET("historico/ver_historico/{nic}")
    fun getHistoricoConsumo(@Path("nic") nic: String): Call<HistoricoResponse>

    @GET("historico/resumen_rapido/{nic}")
    fun getResumenRapido(
        @Path("nic") nic: String,
        @Query("meses") meses: Int
    ): Call<ResumenRapidoResponse>

    @GET("historico/filtrado/{nic}")
    fun getHistoricoFiltrado(
        @Path("nic") nic: String,
        @Query("fecha_desde") fechaDesde: String?,
        @Query("fecha_hasta") fechaHasta: String?,
        @Query("ordenar_por") ordenarPor: String?,
        @Query("orden") orden: String?
    ): Call<HistoricoFiltradoResponse>

    @GET("historico/por_periodo/{nic}")
    fun getHistoricoPorPeriodo(
        @Path("nic") nic: String,
        @Query("periodo") periodo: String
    ): Call<HistoricoResponse>
}
