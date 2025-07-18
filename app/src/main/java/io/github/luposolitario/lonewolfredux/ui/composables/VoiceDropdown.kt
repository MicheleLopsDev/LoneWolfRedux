// In: main/java/io/github/luposolitario/lonewolfredux/ui/composables/VoiceDropdown.kt

package io.github.luposolitario.lonewolfredux.ui.composables

import android.speech.tts.Voice
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceDropdown(
    label: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedValue: String?,
    availableVoices: List<Voice>,
    onVoiceSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedValue ?: "Default di sistema",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            // Aggiungi un'opzione per tornare al default
            DropdownMenuItem(
                text = { Text("Default di sistema") },
                onClick = {
                    onVoiceSelected("") // Passa una stringa vuota per indicare il default
                    onExpandedChange(false)
                }
            )
            // Aggiungi tutte le altre voci disponibili
            availableVoices.forEach { voice ->
                DropdownMenuItem(
                    text = { Text("${voice.name} (${voice.locale.displayLanguage})") },
                    onClick = {
                        onVoiceSelected(voice.name)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}