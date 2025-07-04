package ar.um.econsumo.ui.anomalias

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ar.um.econsumo.data.AnomaliaInfo
import ar.um.econsumo.di.AppDependencies
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnomaliaListScreen(navController: NavController, nic: String) {
    val viewModel = remember { AppDependencies.getAnomaliaViewModel() }
    val listUiState by viewModel.listUiState.collectAsState()

    // Cargar datos al entrar a la pantalla
    LaunchedEffect(key1 = nic) {
        viewModel.obtenerTodasLasAnomalias(nic, true) // Usar JWT para mayor seguridad
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Anomalías") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (listUiState) {
                is AnomaliasListUIState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is AnomaliasListUIState.Error -> {
                    val errorState = listUiState as AnomaliasListUIState.Error

                    ErrorCard(
                        mensaje = errorState.message,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is AnomaliasListUIState.Success -> {
                    val anomaliasData = (listUiState as AnomaliasListUIState.Success).data

                    // Tarjeta de información general
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "NIC: ${anomaliasData.nic}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${anomaliasData.resumen.totalAnomalias} anomalías encontradas",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // Mostrar resumen de estadísticas
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Periodo analizado: ${anomaliasData.resumen.periodoAnalizado}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Normal: ${anomaliasData.resumen.consumosNormales}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Anomalías: ${anomaliasData.resumen.totalAnomalias} (${anomaliasData.resumen.porcentajeAnomalias}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (anomaliasData.anomalias.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No se encontraron anomalías",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(anomaliasData.anomalias) { anomalia ->
                                AnomaliaItemCard(anomalia = anomalia)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnomaliaItemCard(anomalia: AnomaliaInfo) {
    // Usar la nueva propiedad esAnomalia que es Boolean (o usar false como default si es null)
    val esAnomalia = anomalia.esAnomalia ?: false

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esAnomalia)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera con fecha e icono
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Fecha formateada
                val fechaFormateada = anomalia.fecha?.let { fechaStr ->
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val date = inputFormat.parse(fechaStr)
                        outputFormat.format(date ?: Date())
                    } catch (e: Exception) {
                        fechaStr
                    }
                } ?: "Fecha desconocida"

                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (esAnomalia)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Usar el icono devuelto del backend si está disponible
                if (esAnomalia) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Anomalía",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Datos de consumo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Consumo:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (esAnomalia)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${anomalia.consumoKwh ?: "N/A"} kWh",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (esAnomalia)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Mostrar tipo de anomalía si está disponible
            if (!anomalia.tipoAnomalia.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tipo:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (esAnomalia)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = anomalia.tipoAnomalia,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (esAnomalia)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Variación porcentual (ahora se llama variacionTrimestre)
            anomalia.variacionTrimestre?.let { variacion ->
                val esPositivo = variacion > 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Variación:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (esAnomalia)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${if (esPositivo) "+" else ""}${String.format("%.2f", variacion)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (esAnomalia)
                            MaterialTheme.colorScheme.error
                        else if (esPositivo)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Mostrar score de anomalía si está disponible
            anomalia.scoreAnomalia?.let { score ->
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Score:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (esAnomalia)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = String.format("%.2f", score),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (esAnomalia)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mensaje explicativo
            anomalia.mensaje?.let { mensaje ->
                if (mensaje.isNotBlank()) {
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (esAnomalia)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
