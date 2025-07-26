package io.github.luposolitario.lonewolfredux.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.luposolitario.lonewolfredux.viewmodel.DownloadState
import io.github.luposolitario.lonewolfredux.viewmodel.LlmManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmManagerScreen(
    viewModel: LlmManagerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val isModelDownloaded by viewModel.isModelDownloaded.collectAsState()
    val isTokenPresent = uiState.huggingFaceToken.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gestione Modello IA") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (isTokenPresent) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Modello Traduzione Avanzata",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("gemma-3n-E4B-it-int4", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Modello di traduzione ottimizzato.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Contenuto animato per il pulsante/progresso
                    AnimatedContent(targetState = if (isModelDownloaded) DownloadState.Completed else downloadState) { state ->
                        when (state) {
                            is DownloadState.Idle -> {
                                Button(onClick = { viewModel.startModelDownload() }) {
                                    Icon(Icons.Default.Download, contentDescription = "Download")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Scarica")
                                }
                            }

                            is DownloadState.Downloading -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(progress = state.progress / 100f)
                                    Text(
                                        "${state.progress}%",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            is DownloadState.Completed -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completato",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            is DownloadState.Failed -> {
                                Button(onClick = { viewModel.startModelDownload() }) {
                                    Text("Riprova")
                                }
                            }
                        }
                    }
                }


                Text(
                    text = "Configurazione Globale",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = uiState.huggingFaceToken,
                    onValueChange = viewModel::onTokenChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Token Hugging Face") },
                    singleLine = true
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Parametri di Inferenza (Gemma)",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = uiState.temperature,
                    onValueChange = viewModel::onTemperatureChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Temperatura (es. 0.9)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.topK,
                    onValueChange = viewModel::onTopKChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Top-K (es. 50)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.topP,
                    onValueChange = viewModel::onTopPChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Top-P (es. 1.0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.maxLength,
                    onValueChange = viewModel::onMaxLengthChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lunghezza Massima Risposta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}