package ar.um.econsumo.ui.anomalias

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ar.um.econsumo.data.AlertaInfo
import ar.um.econsumo.data.ResumenConsumo
import ar.um.econsumo.di.AppDependencies
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnomaliaScreen(navController: NavController, nic: String) {
    val viewModel = remember { AppDependencies.getAnomaliaViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    // Cargar datos al entrar a la pantalla
    LaunchedEffect(key1 = nic) {
        viewModel.consultarConsumo(nic)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consumo Eléctrico") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (uiState) {
                is AnomaliaUIState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is AnomaliaUIState.Error -> {
                    val errorState = uiState as AnomaliaUIState.Error

                    ErrorCard(
                        mensaje = errorState.message,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is AnomaliaUIState.Success -> {
                    val consumoData = (uiState as AnomaliaUIState.Success).data

                    // Información del NIC
                    NicInfoCard(
                        nic = consumoData.nic,
                        usuario = consumoData.usuario,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Información de la alerta actual
                    AlertaCard(
                        resumen = consumoData.resumen,
                        alertaActual = consumoData.alertaActual,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Botón para ver todas las anomalías
                    Button(
                        onClick = {
                            navController.navigate("anomalias_lista/${consumoData.nic}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Ver historial",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver todas las anomalías (${consumoData.resumen.totalAnomalias})")
                    }
                }
            }
        }
    }
}

@Composable
fun NicInfoCard(
    nic: String,
    usuario: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NIC: $nic",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Usuario: $usuario",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AlertaCard(
    resumen: ResumenConsumo,
    alertaActual: AlertaInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Estado de anomalía
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (resumen.tieneAnomaliaActual) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Advertencia",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¡ANOMALÍA DETECTADA!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "Consumo Normal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información del último consumo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Último consumo registrado:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (resumen.ultimoConsumo != null && resumen.fechaUltimo != null) {
                    val fechaFormateada = try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val date = inputFormat.parse(resumen.fechaUltimo)
                        outputFormat.format(date ?: Date())
                    } catch (e: Exception) {
                        resumen.fechaUltimo
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Fecha:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = fechaFormateada,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Consumo:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${resumen.ultimoConsumo} kWh",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (resumen.variacionPorcentual != null) {
                        val variacion = resumen.variacionPorcentual
                        val esPositivo = variacion > 0
                        val colorVariacion = when {
                            esPositivo -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Variación vs. trimestre anterior:",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "${if (esPositivo) "+" else ""}${String.format("%.2f", variacion)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorVariacion
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No hay datos de consumo disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje de explicación
            if (!alertaActual.mensaje.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (resumen.tieneAnomaliaActual)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = alertaActual.mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        color = if (resumen.tieneAnomaliaActual)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorCard(mensaje: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Error",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
