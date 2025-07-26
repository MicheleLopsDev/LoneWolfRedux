package io.github.luposolitario.lonewolfredux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.luposolitario.lonewolfredux.ui.theme.LoneWolfReduxTheme
import io.github.luposolitario.lonewolfredux.viewmodel.LlmManagerViewModel

class LlmManagerActivity : ComponentActivity() {

    private val viewModel: LlmManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoneWolfReduxTheme {
                LlmManagerScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmManagerScreen(viewModel: LlmManagerViewModel) {
    val huggingFaceToken by viewModel.huggingFaceToken.collectAsState()
    val modelFilePath by viewModel.dmModelFilePath.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    var textFieldValue by remember(huggingFaceToken) { mutableStateOf(huggingFaceToken) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gestione Modello IA") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            Text("Gestione Modello IA (Gemma)", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text("Token Hugging Face") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.saveHuggingFaceToken(textFieldValue) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Salva Token")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Modelli Disponibili", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(availableModels) { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(model.name, style = MaterialTheme.typography.bodyLarge)
                            Text(model.size, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        val isModelDownloaded = modelFilePath.isNotEmpty()

                        if (isModelDownloaded) {
                            Button(
                                onClick = { viewModel.deleteDownloadedModel() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Elimina")
                            }
                        } else {
                            Button(onClick = { viewModel.downloadModel(model) }) {
                                Text("Scarica")
                            }
                        }
                    }
                }
            }
        }
    }
}
