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
            .requestServerAuthCode(webClientId, false) // Forzar que siempre solicite consentimiento
            .requestScopes(
                Scope("https://www.googleapis.com/auth/gmail.readonly"),  // Scope para leer emails
                Scope("https://www.googleapis.com/auth/userinfo.email"),  // Email explícito
                Scope("https://www.googleapis.com/auth/userinfo.profile") // Perfil explícito
            )
            .build()
    }

    // Forzar nuevo consentimiento si es necesario
    LaunchedEffect(Unit) {
        // Verificar si ya tenemos permisos adecuados
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            val hasRequiredScopes = GoogleSignIn.hasPermissions(
                account,
                Scope("https://www.googleapis.com/auth/gmail.readonly"),
                Scope("https://www.googleapis.com/auth/userinfo.email"),
                Scope("https://www.googleapis.com/auth/userinfo.profile")
            )

            // Si no tenemos todos los permisos, cerrar sesión para forzar nueva solicitud
            if (!hasRequiredScopes) {
                Log.d("GoogleAuthScreen", "No se tienen todos los permisos necesarios, cerrando sesión...")
                GoogleSignIn.getClient(context, gso).signOut()
            }
        }
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            // Log para debug
            Log.d("GoogleAuthScreen", "Autenticación exitosa: ${account.email}")

            account?.let {
                val userEmail = it.email ?: return@let
                val serverAuthCode = it.serverAuthCode
                val idToken = it.idToken

                Log.d("GoogleAuthScreen", "Email: $userEmail, ID Token: ${idToken != null}")

                if (idToken != null) {
                    // Usar el nuevo endpoint específico para Android
                    viewModel.androidOAuth(userEmail, idToken, serverAuthCode)
                } else {
                    viewModel.setError("No se pudo obtener el token de ID")
                }
            }
        } catch (e: ApiException) {
            // Log para debug
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
