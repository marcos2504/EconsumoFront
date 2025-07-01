package ar.um.econsumo.ui.dashboard

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ar.um.econsumo.di.AppDependencies

private const val TAG = "NicSelectorScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NicSelectorScreen(navController: NavController) {
    val context = LocalContext.current
    // Obtenemos el ViewModel desde AppDependencies, pasando el contexto
    val viewModel = remember { AppDependencies.getNicSelectorViewModel(context) }

    // Estados del ViewModel
    val state by viewModel.state.collectAsState()
    val selectedNic by viewModel.selectedNic.collectAsState()

    // Estado local para el menú desplegable
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selección de NIC") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "👤 Seleccioná tu NIC",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Seleccioná el NIC para consultar información sobre tu consumo eléctrico y facturas.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Contenido condicional según el estado
                    when (state) {
                        is NicSelectorState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando NICs disponibles...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        is NicSelectorState.Error -> {
                            Text(
                                text = "Error: ${(state as NicSelectorState.Error).message}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.loadNics() }
                            ) {
                                Text("Reintentar")
                            }
                        }

                        is NicSelectorState.Success -> {
                            val nics = (state as NicSelectorState.Success).nics

                            if (nics.isEmpty()) {
                                Text(
                                    text = "No se encontraron NICs disponibles",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                // Menú desplegable para seleccionar el NIC
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedNic ?: "",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("NIC") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        nics.forEach { nic ->
                                            DropdownMenuItem(
                                                text = { Text(nic) },
                                                onClick = {
                                                    viewModel.selectNic(nic)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Botones para navegar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (viewModel.isNicSelected()) {
                                                navController.navigate("consumo_historico/${viewModel.getSelectedNic()}")
                                            } else {
                                                Toast.makeText(context, "Seleccioná un NIC válido", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Ver Consumo")
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Nuevo botón para consultar anomalías con JWT
                                Button(
                                    onClick = {
                                        if (viewModel.isNicSelected()) {
                                            navController.navigate("anomalias/${viewModel.getSelectedNic()}")
                                        } else {
                                            Toast.makeText(context, "Seleccioná un NIC válido", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Consultar Anomalías", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (viewModel.isNicSelected()) {
                                            navController.navigate("dashboard?nic=${viewModel.getSelectedNic()}")
                                        } else {
                                            Toast.makeText(context, "Seleccioná un NIC válido", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Dashboard")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
