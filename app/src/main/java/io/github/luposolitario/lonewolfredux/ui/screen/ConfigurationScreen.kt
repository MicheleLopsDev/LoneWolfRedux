package io.github.luposolitario.lonewolfredux.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.luposolitario.lonewolfredux.viewmodel.ConfigurationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(viewModel: ConfigurationViewModel) {

    val showDialog by viewModel.showResetConfirmationDialog.collectAsState()

    // Dialogo di conferma per il reset totale
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onResetTotalCancelled() },
            title = { Text("Conferma Reset Totale") },
            text = { Text("Sei sicuro di voler cancellare TUTTI i dati di gioco? L'operazione non è reversibile.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onResetTotalConfirmed() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sì, Cancella Tutto")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onResetTotalCancelled() }) {
                    Text("Annulla")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Impostazioni") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Azioni Partita", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Pulsante per il Reset Totale
            Button(
                onClick = { viewModel.onResetTotalClicked() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text("Reset Totale", color = MaterialTheme.colorScheme.onErrorContainer)
            }
            Text(
                "Cancella tutti i salvataggi e i progressi per tutti i libri.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}