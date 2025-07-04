package ar.um.econsumo.ui.dashboard

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ar.um.econsumo.data.SelectorItem
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
    val selectedItem by viewModel.selectedItem.collectAsState()

    // Estado local para el menú desplegable
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selección de Propiedad") },
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
                        text = "👤 Seleccioná tu propiedad",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Elegí la propiedad para consultar información sobre tu consumo eléctrico y facturas.",
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
                                text = "Cargando propiedades disponibles...",
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
                            val selectorItems = (state as NicSelectorState.Success).selectorItems

                            if (selectorItems.isEmpty()) {
                                Text(
                                    text = "No se encontraron propiedades disponibles",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                // Dropdown mejorado para seleccionar propiedad
                                PropertySelectorDropdown(
                                    items = selectorItems,
                                    selectedItem = selectedItem,
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it },
                                    onItemSelected = { item ->
                                        viewModel.selectNic(item.value, item)
                                        expanded = false
                                    }
                                )

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
                                                Toast.makeText(context, "Seleccioná una propiedad válida", Toast.LENGTH_SHORT).show()
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

                                // Botón para consultar anomalías
                                Button(
                                    onClick = {
                                        if (viewModel.isNicSelected()) {
                                            navController.navigate("anomalias/${viewModel.getSelectedNic()}")
                                        } else {
                                            Toast.makeText(context, "Seleccioná una propiedad válida", Toast.LENGTH_SHORT).show()
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

                                // Botón para volver a sincronizar facturas
                                Button(
                                    onClick = {
                                        // Navegar a la sincronización con parámetro forzar_sync=true
                                        navController.navigate("sync?forzar_sync=true")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sincronizar más facturas")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertySelectorDropdown(
    items: List<SelectorItem>,
    selectedItem: SelectorItem?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onItemSelected: (SelectorItem) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Dropdown mejorado para seleccionar propiedad
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { onExpandedChange(it) },
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                value = selectedItem?.label ?: "Seleccionar propiedad",
                onValueChange = {},
                label = { Text("Seleccionar propiedad") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                items.forEach { item ->
                    PropertyDropdownItem(
                        item = item,
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun PropertyDropdownItem(
    item: SelectorItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
