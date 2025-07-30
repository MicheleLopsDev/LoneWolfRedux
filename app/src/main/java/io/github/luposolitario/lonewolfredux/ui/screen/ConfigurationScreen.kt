package io.github.luposolitario.lonewolfredux.ui.screen

import android.speech.tts.Voice
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.luposolitario.lonewolfredux.service.TtsService
import io.github.luposolitario.lonewolfredux.ui.composables.VoiceDropdown
import io.github.luposolitario.lonewolfredux.viewmodel.ConfigurationViewModel


// --- CLASSE SPOSTATA QUI FUORI ---
// Ora è visibile a tutto il file.
sealed interface TtsUiState {
    object Loading : TtsUiState
    data class Ready(val voices: List<Voice>) : TtsUiState
    object Unavailable : TtsUiState
}
// ------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(viewModel: ConfigurationViewModel) {

    val showDialog by viewModel.showResetConfirmationDialog.collectAsState()
    val targetLanguage by viewModel.targetLanguage.collectAsState()
    val languageOptions = viewModel.availableLanguages
    val appSettings by viewModel.appSettings.collectAsState()
    // --- STATI LOCALI PER I CONTROLLI UI ---
    var speechRate by remember { mutableStateOf(1.0f) }
    var pitch by remember { mutableStateOf(1.0f) }
    var langMenuExpanded by remember { mutableStateOf(false) }
    var availableVoices by remember { mutableStateOf<List<Voice>>(emptyList()) }
    var isNarratorDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var ttsState by remember { mutableStateOf<TtsUiState>(TtsUiState.Loading) }

    LaunchedEffect(appSettings) {
        speechRate = if (appSettings.ttsSpeechRate == 0f) 1.0f else appSettings.ttsSpeechRate
        pitch = if (appSettings.ttsPitch == 0f) 1.0f else appSettings.ttsPitch
    }

    DisposableEffect(context) {
        var ttsService: TtsService? = null
        ttsService = TtsService(context) { success ->
            ttsState = if (success) {
                TtsUiState.Ready(ttsService?.getAvailableVoices() ?: emptyList())
            } else {
                TtsUiState.Unavailable
            }
        }
        onDispose {
            ttsService.shutdown()
        }
    }


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


            // --- NUOVO BLOCCO: PULSANTE CANCELLA CACHE ---
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onClearTranslationCacheClicked() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text("Svuota Cache Traduzioni", color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Text(
                "Cancella tutte le traduzioni salvate sul dispositivo. Questo può liberare spazio e risolvere eventuali errori di traduzione.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            // --- FINE BLOCCO ---

            // --- SEZIONE AUDIO (TTS) ---
            Spacer(Modifier.height(24.dp)); Divider(); Spacer(Modifier.height(16.dp))
            Text("Audio (Sintesi Vocale)", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Velocità Voce", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            Slider(
                value = speechRate,
                onValueChange = { speechRate = it },
                onValueChangeFinished = { viewModel.setSpeechRate(speechRate) },
                valueRange = 0.5f..2.0f
            )

            Text("Tono Voce", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            Slider(
                value = pitch,
                onValueChange = { pitch = it },
                onValueChangeFinished = { viewModel.setPitch(pitch) },
                valueRange = 0.5f..2.0f
            )

            Spacer(Modifier.height(16.dp))

            // Il dropdown ora reagisce correttamente allo stato
            when (val state = ttsState) {
                is TtsUiState.Loading -> {
                    OutlinedTextField(
                        value = "Caricamento voci...",
                        onValueChange = {},
                        label = { Text("Voce del Narratore") },
                        enabled = false,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is TtsUiState.Unavailable -> {
                    OutlinedTextField(
                        value = "Servizio non disponibile",
                        onValueChange = {},
                        label = { Text("Voce del Narratore") },
                        enabled = false,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is TtsUiState.Ready -> {
                    VoiceDropdown(
                        label = "Voce del Narratore",
                        expanded = isNarratorDropdownExpanded,
                        onExpandedChange = { isNarratorDropdownExpanded = it },
                        selectedValue = appSettings.ttsNarratorVoice.ifEmpty { null },
                        availableVoices = state.voices,
                        onVoiceSelected = { voiceName ->
                            viewModel.setNarratorVoice(voiceName)
                        }
                    )
                }
            }

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

            val fontZoomLevel by viewModel.fontZoomLevel.collectAsState()

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Accessibilità", style = MaterialTheme.typography.titleLarge)

            Text(
                text = "Dimensione Testo (${fontZoomLevel}%)",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Slider(
                value = fontZoomLevel.toFloat(),
                onValueChange = { newValue ->
                    viewModel.setFontZoomLevel(newValue.toInt())
                },
                valueRange = 75f..200f, // L'utente può scegliere tra 75% e 200%
                steps = 24 // Numero di "scatti" intermedi per lo slider
            )
        }
    }
}