package ar.um.econsumo.ui.sync

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.um.econsumo.data.EstadoSyncResponse

/**
 * Pantalla de sincronización de facturas desde Gmail
 * @param viewModel ViewModel para la pantalla de sincronización
 * @param onNavigateToNicSelector Función para navegar a la pantalla de selección de NIC
 * @param forzarSync Si se debe forzar la sincronización aunque ya haya facturas (por defecto false)
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SyncScreen(
    viewModel: SyncViewModel,
    onNavigateToNicSelector: () -> Unit,
    forzarSync: Boolean = false
) {
    val syncState by viewModel.syncState.collectAsState()
    val context = LocalContext.current

    // Número de emails a procesar (valor seleccionado)
    var selectedEmailCount by remember { mutableStateOf(10) }

    // Lista de opciones disponibles
    val emailOptions = listOf(5, 10, 15, 20, 30)

    // Verificar el estado de sincronización al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.verificarEstadoSync()
    }

    LaunchedEffect(key1 = syncState) {
        when (syncState) {
            is SyncState.Success -> {
                val response = (syncState as SyncState.Success).response
                val message = "¡Sincronización exitosa! Se encontraron ${response.facturasEncontradas} facturas"
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                // Navegar a la pantalla de selección de NIC después de una sincronización exitosa
                onNavigateToNicSelector()
            }
            is SyncState.Error -> {
                val message = "Error: ${(syncState as SyncState.Error).message}"
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
            is SyncState.EstadoVerificado -> {
                val estadoSync = (syncState as SyncState.EstadoVerificado).estado

                // Si el usuario ya tiene facturas y no necesita sincronización inicial,
                // Y NO estamos forzando la sincronización, navegar directamente al selector
                if (!estadoSync.necesitaSyncInicial && estadoSync.tieneFacturas && !forzarSync) {
                    val message = "Ya tienes ${estadoSync.totalFacturas} facturas sincronizadas"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    onNavigateToNicSelector()
                } else if (forzarSync && estadoSync.tieneFacturas) {
                    // Si estamos forzando la sincronización, mostrar un mensaje pero no redirigir
                    val message = "Tienes ${estadoSync.totalFacturas} facturas. Puedes sincronizar más."
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sincronización de Facturas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bienvenida al usuario
                Text(
                    text = "Hola, ${viewModel.getUserName()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Mostrar información diferente según el estado de verificación
                if (syncState is SyncState.EstadoVerificado) {
                    val estadoSync = (syncState as SyncState.EstadoVerificado).estado
                    EstadoSyncInfo(estadoSync)
                }

                // Card de información
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "Sincronización de facturas",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Esta aplicación te permitirá sincronizar tus facturas de luz desde tu cuenta de Gmail.",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Selecciona cuántos emails deseas procesar para buscar facturas:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de cantidad de emails
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "Cantidad de emails a procesar",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Opciones para la cantidad de emails
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            emailOptions.forEach { option ->
                                EmailOptionButton(
                                    count = option,
                                    isSelected = selectedEmailCount == option,
                                    onClick = { selectedEmailCount = option }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Se procesarán $selectedEmailCount emails en busca de facturas.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de sincronización
                Button(
                    onClick = {
                        // Usar el nuevo endpoint de sincronización inteligente
                        viewModel.syncInteligente(selectedEmailCount)
                    },
                    enabled = syncState !is SyncState.Loading && syncState !is SyncState.Checking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (syncState is SyncState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (syncState is SyncState.Loading) "Sincronizando..." else "Iniciar Sincronización",
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para ir directamente al selector de NIC (omitir sincronización)
                TextButton(
                    onClick = {
                        onNavigateToNicSelector()
                    }
                ) {
                    Text("Ir directamente a mis facturas")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Indicador de carga durante la sincronización o verificación
            if (syncState is SyncState.Loading || syncState is SyncState.Checking) {
                // Crear un temporizador para mostrar tiempo transcurrido
                var secondsElapsed by remember { mutableStateOf(0) }
                var currentMessage by remember {
                    mutableStateOf(
                        if (syncState is SyncState.Loading)
                            "Iniciando sincronización..."
                        else
                            "Verificando estado..."
                    )
                }

                // Actualizar el contador cada segundo y cambiar mensajes informativos
                LaunchedEffect(syncState) {
                    while(true) {
                        kotlinx.coroutines.delay(1000)
                        secondsElapsed++

                        // Actualizar mensajes informativos basados en el tiempo transcurrido
                        if (syncState is SyncState.Loading) {
                            currentMessage = when {
                                secondsElapsed < 10 -> "Iniciando sincronización..."
                                secondsElapsed < 30 -> "Conectando con Gmail..."
                                secondsElapsed < 60 -> "Buscando facturas en tus emails..."
                                secondsElapsed < 120 -> "Procesando emails, esto puede tardar unos minutos..."
                                secondsElapsed < 180 -> "Analizando contenido de los emails..."
                                secondsElapsed < 240 -> "Extrayendo datos de facturas..."
                                else -> "Casi listo, finalizando sincronización..."
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .width(320.dp)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            val statusText = if (syncState is SyncState.Loading)
                                "Sincronizando facturas"
                            else
                                "Verificando estado"

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = currentMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (syncState is SyncState.Loading) {
                                Text(
                                    text = "Procesando $selectedEmailCount emails de Gmail",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Mostrar tiempo transcurrido
                                val minutes = secondsElapsed / 60
                                val seconds = secondsElapsed % 60
                                Text(
                                    text = "Tiempo transcurrido: ${String.format("%02d:%02d", minutes, seconds)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Información importante para el usuario
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Este proceso puede tardar hasta 5 minutos dependiendo del número de emails. Por favor, no cierres la aplicación.",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstadoSyncInfo(estado: EstadoSyncResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (estado.tieneFacturas) Icons.Default.CheckCircle else Icons.Default.Refresh,
                    contentDescription = null,
                    tint = if (estado.tieneFacturas) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = if (estado.tieneFacturas) "Ya tienes facturas" else "Se necesita sincronización inicial",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (estado.tieneFacturas) {
                Text("Tienes ${estado.totalFacturas} facturas sincronizadas")
                Spacer(modifier = Modifier.height(4.dp))

                estado.ultimaFactura?.let {
                    Text("Última factura: ${it.fecha} (NIC: ${it.nic})")
                }
            } else {
                Text("Necesitas sincronizar para poder usar todas las funcionalidades de la app")
            }
        }
    }
}

/**
 * Botón para seleccionar la cantidad de emails a procesar
 */
@Composable
fun EmailOptionButton(
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = if (isSelected) 0.dp else 1.dp
        )
    ) {
        Text(count.toString())
    }
}
