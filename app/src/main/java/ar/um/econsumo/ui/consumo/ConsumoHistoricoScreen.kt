package ar.um.econsumo.ui.consumo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ar.um.econsumo.data.*
import ar.um.econsumo.di.AppDependencies
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumoHistoricoScreen(navController: NavHostController, nic: String) {
    val viewModel: ConsumoHistoricoViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val filtros by viewModel.filtros.collectAsState()

    val periodoOptions = listOf("Completo", "Últimos 3 meses", "Últimos 6 meses", "Último año")
    var selectedPeriodoOption by remember { mutableStateOf(periodoOptions[0]) }

    // Cargar datos al inicio
    LaunchedEffect(nic) {
        viewModel.cargarHistorico(nic)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consumo Histórico - NIC: $nic") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Icono de flecha atrás
                        Text("<", fontSize = 24.sp)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Filtro de períodos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Seleccionar período",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Dropdown menu para seleccionar período
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedPeriodoOption)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        periodoOptions.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    selectedPeriodoOption = opcion
                                    expanded = false

                                    // Cargar datos según el período seleccionado
                                    when(opcion) {
                                        "Últimos 3 meses" -> viewModel.cargarHistoricoPorPeriodo(nic, "ultimos_3_meses")
                                        "Últimos 6 meses" -> viewModel.cargarHistoricoPorPeriodo(nic, "ultimos_6_meses")
                                        "Último año" -> viewModel.cargarHistoricoPorPeriodo(nic, "ultimo_anio")
                                        else -> viewModel.cargarHistorico(nic)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Contenido principal según el estado
            when (uiState) {
                is ConsumoUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ConsumoUiState.Success -> {
                    val data = (uiState as ConsumoUiState.Success).data

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        ConsumoLineChart(data.paraGrafico)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tarjeta de estadísticas
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Estadísticas de Consumo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Promedio:", fontWeight = FontWeight.Medium)
                                    Text("${data.estadisticas.promedio} kWh")
                                }
                                Column {
                                    Text("Consumo Máximo:", fontWeight = FontWeight.Medium)
                                    Text("${data.estadisticas.maximo} kWh")
                                }
                                Column {
                                    Text("Consumo Mínimo:", fontWeight = FontWeight.Medium)
                                    Text("${data.estadisticas.minimo} kWh")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tendencia: ${data.estadisticas.tendencia ?: "estable"}")
                            data.estadisticas.cambioPorcentual?.let {
                                val signo = if (it >= 0) "+" else ""
                                Text("Cambio: $signo$it%")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Detalle Mensual",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    LazyColumn {
                        items(data.datos) { dato ->
                            DatoConsumoItem(dato)
                        }
                    }
                }

                is ConsumoUiState.ResumenRapido -> {
                    val data = (uiState as ConsumoUiState.ResumenRapido).data

                    // Implementar visualización para resumen rápido
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Resumen de Consumo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Estado: ${data.estado}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Último consumo: ${data.ultimoConsumo} kWh (${data.fechaUltimo})")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Promedio reciente: ${data.promedioReciente} kWh")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(vertical = 16.dp)
                    ) {
                        ResumenLineChart(data.datosRecientes)
                    }
                }

                is ConsumoUiState.Filtrado -> {
                    val data = (uiState as ConsumoUiState.Filtrado).data

                    // Aplicar visualización para datos filtrados
                    Text(
                        text = "Datos Filtrados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Text(
                        "Filtros aplicados: ${data.filtrosAplicados.fechaDesde ?: "N/A"} a ${data.filtrosAplicados.fechaHasta ?: "N/A"}"
                    )

                    // Mostrar estadísticas
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Estadísticas del Período",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Promedio:", fontWeight = FontWeight.Medium)
                                    Text("${data.estadisticas.promedio} kWh")
                                }
                                Column {
                                    Text("Máximo:", fontWeight = FontWeight.Medium)
                                    Text("${data.estadisticas.maximo} kWh")
                                }
                                Column {
                                    Text("Mínimo:", fontWeight = FontWeight.Medium)
                                    Text("${data.estadisticas.minimo} kWh")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Total consumo: ${data.estadisticas.totalConsumo ?: 0} kWh")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        items(data.datos) { dato ->
                            DatoConsumoItem(dato)
                        }
                    }
                }

                is ConsumoUiState.Error -> {
                    val errorMsg = (uiState as ConsumoUiState.Error).mensaje
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error al cargar datos",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMsg)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.cargarHistorico(nic) }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatoConsumoItem(dato: DatoConsumo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Fecha: ${dato.fecha}",
                    fontWeight = FontWeight.Medium
                )
                Text("ID Factura: ${dato.facturaId}")
            }

            Text(
                text = "${dato.consumoKwh} kWh",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun ConsumoLineChart(datos: List<DatoGrafico>) {
    val context = LocalContext.current

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.textSize = 14f
                axisLeft.apply {
                    setDrawGridLines(false)
                    axisMinimum = 0f
                }
                axisRight.isEnabled = false
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                }
                setTouchEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { chart ->
            val entries = datos.mapIndexed { index, dato ->
                Entry(index.toFloat(), dato.consumo)
            }

            val labels = datos.map { it.fecha }.toTypedArray()
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

            val dataSet = LineDataSet(entries, "Consumo (kWh)").apply {
                color = android.graphics.Color.BLUE
                setCircleColor(android.graphics.Color.BLUE)
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(false)
            }

            val lineData = LineData(dataSet)
            chart.data = lineData
            chart.invalidate()
        }
    )
}

@Composable
fun ResumenLineChart(datos: List<DatoGrafico>) {
    // Similar a ConsumoLineChart pero adaptado para el formato de resumen
    ConsumoLineChart(datos)
}
