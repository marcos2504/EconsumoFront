package ar.um.econsumo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ar.um.econsumo.di.AppDependencies
import ar.um.econsumo.ui.anomalias.AnomaliaListScreen
import ar.um.econsumo.ui.anomalias.AnomaliaScreen
import ar.um.econsumo.ui.auth.GoogleAuthScreen
import ar.um.econsumo.ui.dashboard.DashboardScreen
import ar.um.econsumo.ui.dashboard.FacturaScreen
import ar.um.econsumo.ui.dashboard.NicSelectorScreen
import ar.um.econsumo.ui.sync.SyncScreen
import ar.um.econsumo.ui.consumo.ConsumoHistoricoScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "auth") {
        // Pantalla de autenticación con Google
        composable("auth") {
            GoogleAuthScreen(
                onNavigateToDashboard = {
                    // Ahora navegamos a la pantalla de sincronización después de la autenticación
                    navController.navigate("sync") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de sincronización
        composable("sync") {
            val context = LocalContext.current
            SyncScreen(
                viewModel = AppDependencies.getSyncViewModel(context),
                onNavigateToNicSelector = {
                    navController.navigate("selector") {
                        // Opcional: popUpTo("sync") { inclusive = true }
                        // Si quieres que el usuario no pueda volver atrás a la pantalla de sincronización
                    }
                }
            )
        }

        // Pantalla de selección de NIC
        composable("selector") {
            NicSelectorScreen(navController)
        }

        // Pantalla principal del dashboard
        composable(
            route = "dashboard?nic={nic}",
            arguments = listOf(navArgument("nic") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val nic = backStackEntry.arguments?.getString("nic") ?: ""
            DashboardScreen(navController, nic)
        }

        // Pantalla de facturas
        composable(
            route = "facturas?nic={nic}",
            arguments = listOf(navArgument("nic") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val nic = backStackEntry.arguments?.getString("nic") ?: ""
            FacturaScreen(navController, nic)
        }

        // Nuevas pantallas de anomalías

        // Pantalla principal de consumo y anomalía actual
        composable(
            route = "anomalias/{nic}",
            arguments = listOf(navArgument("nic") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val nic = backStackEntry.arguments?.getString("nic") ?: ""
            AnomaliaScreen(navController, nic)
        }

        // Pantalla de listado histórico de anomalías
        composable(
            route = "anomalias_lista/{nic}",
            arguments = listOf(navArgument("nic") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val nic = backStackEntry.arguments?.getString("nic") ?: ""
            AnomaliaListScreen(navController, nic)
        }

        // Nueva pantalla para visualizar consumo histórico
        composable(
            route = "consumo_historico/{nic}",
            arguments = listOf(navArgument("nic") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val nic = backStackEntry.arguments?.getString("nic") ?: ""
            ConsumoHistoricoScreen(navController, nic)
        }
    }
}
