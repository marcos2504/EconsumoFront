package ar.um.econsumo.ui.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ar.um.econsumo.R
import ar.um.econsumo.di.AppDependencies
import ar.um.econsumo.ui.components.LoadingScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope

@Composable
fun GoogleAuthScreen(
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { AppDependencies.getAuthViewModel(context) }
    val authState by viewModel.authState.collectAsState()

    // ID de cliente Web de google-services.json
    val webClientId = "575643410417-l5sbvsrooteah5h4bm9f4slm5e1ilnv3.apps.googleusercontent.com"

    // Configuración actualizada para Google Sign-In con scopes necesarios
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestId() // Solicita el ID del usuario
            .requestIdToken(webClientId) // Solicita un token ID para autenticación
            .requestServerAuthCode(webClientId, true) // TRUE para forzar que siempre solicite consentimiento y obtener un serverAuthCode fresco
            .requestScopes(
                Scope("https://www.googleapis.com/auth/gmail.readonly"),  // Scope para leer emails
                Scope("https://www.googleapis.com/auth/userinfo.email"),  // Email explícito
                Scope("https://www.googleapis.com/auth/userinfo.profile") // Perfil explícito
            )
            .build()
    }

    // Forzar nuevo consentimiento siempre para asegurar obtener un server auth code
    LaunchedEffect(Unit) {
        Log.d("GoogleAuthScreen", "Cerrando sesión para forzar nuevo consentimiento y obtener server auth code...")
        GoogleSignIn.getClient(context, gso).signOut()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)

            account?.let {
                val userEmail = it.email ?: return@let
                val serverAuthCode = it.serverAuthCode
                val idToken = it.idToken

                // Logs detallados para depuración
                Log.d("GoogleAuthScreen", "Autenticación exitosa: $userEmail")
                Log.d("GoogleAuthScreen", "ID Token disponible: ${idToken != null}")

                if (serverAuthCode != null) {
                    Log.d("GoogleAuthScreen", "Server Auth Code obtenido (${serverAuthCode.length} caracteres): ${serverAuthCode.take(10)}...")
                } else {
                    Log.e("GoogleAuthScreen", "¡NO SE OBTUVO SERVER AUTH CODE! Verifica que requestServerAuthCode tiene el parámetro force=true")
                }

                if (idToken != null && serverAuthCode != null) {
                    // Enviar tanto el idToken como el serverAuthCode al backend
                    Log.d("GoogleAuthScreen", "Enviando idToken y serverAuthCode al backend...")
                    viewModel.androidOAuth(userEmail, idToken, serverAuthCode)
                } else {
                    if (idToken == null) {
                        viewModel.setError("No se pudo obtener el ID token necesario")
                    } else {
                        viewModel.setError("No se pudo obtener el server auth code necesario")
                    }
                }
            }
        } catch (e: ApiException) {
            Log.e("GoogleAuthScreen", "Error de autenticación: ${e.statusCode}", e)

            // Manejo especial para el error 12501 (usuario canceló)
            if (e.statusCode == CommonStatusCodes.CANCELED) {
                Log.d("GoogleAuthScreen", "El usuario canceló el inicio de sesión")
                viewModel.setError("Inicio de sesión cancelado por el usuario")
            } else {
                viewModel.setError("Error al iniciar sesión con Google: ${e.statusCode}")
            }
        }
    }

    // Observa cambios en el estado de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                Log.d("GoogleAuthScreen", "Usuario autenticado, navegando al dashboard")
                onNavigateToDashboard()
            }
            is AuthState.OAuthCompleted -> {
                Log.d("GoogleAuthScreen", "OAuth completado, navegando al dashboard")
                onNavigateToDashboard()
            }
            else -> {
                // No hacemos nada para los otros estados
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "eConsumo",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                // Asegurarse de cerrar cualquier sesión previa
                googleSignInClient.signOut().addOnCompleteListener {
                    // Iniciar el flujo de autenticación
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text("Iniciar sesión con Google")
        }

        // Solo mostramos el error si existe
        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Mostramos el indicador de carga si está cargando
        if (authState is AuthState.Loading) {
            LoadingScreen()
        }
    }
}
