package io.github.luposolitario.lonewolfredux.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.luposolitario.lonewolfredux.viewmodel.LlmManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmManagerScreen(
    viewModel: LlmManagerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

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