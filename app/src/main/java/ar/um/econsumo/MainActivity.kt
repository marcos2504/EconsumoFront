package ar.um.econsumo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ar.um.econsumo.ui.navigation.AppNavigation
import ar.um.econsumo.ui.theme.EconsumoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContent {
                EconsumoTheme {
                    AppNavigation()
                }
            }
        } catch (e: Exception) {
            // Registrar el error para depuración
            Log.e("MainActivity", "Error al iniciar la aplicación", e)

            // Mostrar un mensaje de error
            Toast.makeText(
                this,
                "Ha ocurrido un error al iniciar la aplicación: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
