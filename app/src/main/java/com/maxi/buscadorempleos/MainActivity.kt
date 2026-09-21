package com.maxi.buscadorempleos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    BuscadorEmpleosScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscadorEmpleosScreen() {
    var keyword by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var jobs by remember { mutableStateOf<List<JobOffer>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun buscar() {
        if (keyword.isBlank() || location.isBlank()) {
            dialogMessage = "Por favor, ingresa palabra clave y ubicación."
            return
        }
        loading = true
        scope.launch {
            val resultado = withContext(Dispatchers.IO) {
                JobScraper.buscarTrabajos(keyword.trim(), location.trim(), maxOffers = 10)
            }
            jobs = resultado
            loading = false
            if (resultado.isEmpty()) {
                dialogMessage = "No se encontraron resultados para esa búsqueda."
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Buscador de Empleos") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                label = { Text("Palabra clave") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { buscar() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Buscar")
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(jobs) { job ->
                    JobRow(job = job) {
                        if (job.link != "No disponible") {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(job.link)))
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    dialogMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { dialogMessage = null },
            confirmButton = {
                TextButton(onClick = { dialogMessage = null }) { Text("OK") }
            },
            text = { Text(msg) }
        )
    }
}

@Composable
private fun JobRow(job: JobOffer, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(text = "${job.title} - ${job.company}", style = MaterialTheme.typography.bodyLarge)
    }
}
