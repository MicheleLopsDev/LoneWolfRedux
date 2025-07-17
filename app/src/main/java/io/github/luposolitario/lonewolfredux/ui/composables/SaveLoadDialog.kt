package io.github.luposolitario.lonewolfredux.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.luposolitario.lonewolfredux.datastore.SaveSlotInfo

@Composable
fun SaveLoadDialog(
    slots: List<SaveSlotInfo>,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
    onLoad: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Salva / Carica Partita", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                slots.forEach { slotInfo ->
                    SlotRow(
                        slotInfo = slotInfo,
                        onSave = { onSave(slotInfo.slotId) },
                        onLoad = { onLoad(slotInfo.slotId) },
                    )
                    Divider()
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Annulla")
                }
            }
        }
    }
}

@Composable
fun SlotRow(
    slotInfo: SaveSlotInfo,
    onSave: () -> Unit,
    onLoad: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Slot ${slotInfo.slotId}", style = MaterialTheme.typography.bodyLarge)
            Text(text = slotInfo.lastSaved, style = MaterialTheme.typography.bodySmall)
        }
        Row {
            Button(onClick = onSave) {
                Text("Salva")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = { onLoad() },
                enabled = !slotInfo.isEmpty
            ) {
                Text("Carica")
            }
        }
    }
}