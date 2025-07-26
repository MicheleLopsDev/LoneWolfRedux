package io.github.luposolitario.lonewolfredux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.luposolitario.lonewolfredux.viewmodel.LlmManagerViewModel

class LlmManagerActivity : ComponentActivity() {
    
    private val viewModel: LlmManagerViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LlmManagerScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun LlmManagerScreen(viewModel: LlmManagerViewModel) {
    val huggingFaceToken by viewModel.huggingFaceToken.collectAsState()
    var textFieldValue by remember(huggingFaceToken) { mutableStateOf(huggingFaceToken) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Gestione Modello IA (Gemma)")

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
    }
}
