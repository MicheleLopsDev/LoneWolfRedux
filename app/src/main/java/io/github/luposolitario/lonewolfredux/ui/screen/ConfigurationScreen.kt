package io.github.luposolitario.lonewolfredux.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.luposolitario.lonewolfredux.viewmodel.ConfigurationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(viewModel: ConfigurationViewModel) {

    val showDialog by viewModel.showResetConfirmationDialog.collectAsState()
    val useAdvancedTranslation by viewModel.isAdvancedTranslationEnabled.collectAsState()
    val targetLanguage by viewModel.targetLanguage.collectAsState()
    val languageOptions = viewModel.availableLanguages
    var langMenuExpanded by remember { mutableStateOf(false) }
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




            Text("Motore di Traduzione", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // --- INIZIO BLOCCO NUOVO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Traduzione Avanzata (Gemma)", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Usa il modello IA locale per una traduzione di qualità superiore. Più lento e consuma più batteria. Richiede il download del modello IA.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = useAdvancedTranslation,
                    onCheckedChange = { viewModel.setUseAdvancedTranslation(it) },
                    // Per ora lo disabilitiamo, come da piano, finché non implementeremo il motore Gemma
                    enabled = false
                )
            }
            // --- FINE BLOCCO NUOVO ---

            Spacer(modifier = Modifier.height(16.dp))
            Text("Lingua di Traduzione", style = MaterialTheme.typography.titleLarge)

            ExposedDropdownMenuBox(
                expanded = langMenuExpanded,
                onExpandedChange = { langMenuExpanded = !langMenuExpanded }
            ) {
                OutlinedTextField(
                    value = languageOptions[targetLanguage] ?: "Italiano",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = langMenuExpanded,
                    onDismissRequest = { langMenuExpanded = false }
                ) {
                    languageOptions.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                viewModel.setTargetLanguage(code)
                                langMenuExpanded = false
                            }
                        )
                    }
                }
            }


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