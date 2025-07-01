package ar.um.econsumo.ui.dashboard

import ar.um.econsumo.data.RetrofitClient

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ar.um.econsumo.data.Factura

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun FacturaScreen(navController: NavController, nic: String) {
    val context = LocalContext.current
    var nicSeleccionado by remember { mutableStateOf(nic) }
    var facturas by remember { mutableStateOf<List<Factura>>(emptyList()) }
    val listaNics = remember { mutableStateListOf<String>() }
    var expanded by remember { mutableStateOf(false) }

    // Cargar lista NICs una vez
    LaunchedEffect(Unit) {
        RetrofitClient.instance.getTodosLosNics().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    listaNics.clear()
                    listaNics.addAll(response.body() ?: emptyList())
                }
            }
            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Cargar facturas cuando cambie nicSeleccionado
    LaunchedEffect(nicSeleccionado) {
        RetrofitClient.instance.getFacturas(nicSeleccionado).enqueue(object : Callback<List<Factura>> {
            override fun onResponse(call: Call<List<Factura>>, response: Response<List<Factura>>) {
                facturas = response.body() ?: emptyList()
            }
            override fun onFailure(call: Call<List<Factura>>, t: Throwable) {
                Toast.makeText(context, "Error al cargar facturas: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Seleccionar NIC", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nicSeleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text("NIC") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listaNics.forEach { nicItem ->
                DropdownMenuItem(
                    text = { Text(nicItem) },
                    onClick = {
                        nicSeleccionado = nicItem
                        expanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Facturas para NIC: $nicSeleccionado", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            facturas.forEach { factura ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Fecha: ${factura.fecha}")
                        Text("Consumo: ${factura.consumo_kwh} kWh")
                        Text("NIC: ${factura.nic}")
                    }
                }
            }
        }
    }
}



