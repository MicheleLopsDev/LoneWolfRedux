package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.luposolitario.lonewolfredux.data.model.DownloadableModel
import io.github.luposolitario.lonewolfredux.datastore.modelSettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LlmManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val modelSettingsDataStore = application.modelSettingsDataStore

    val huggingFaceToken: StateFlow<String> = modelSettingsDataStore.data
        .map { it.huggingFaceToken }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val dmModelFilePath: StateFlow<String> = modelSettingsDataStore.data
        .map { it.dmModelFilePath }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val narrativeTone: StateFlow<String> = modelSettingsDataStore.data
        .map { it.narrativeTone }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val availableModels: StateFlow<List<DownloadableModel>> = kotlinx.coroutines.flow.flowOf(
        listOf(
            DownloadableModel(
                name = "Gemma 3n-E4-B-it",
                size = "2.5 GB",
                id = "gemma-3n-E4-B-it-int4"
            )
        )
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveHuggingFaceToken(token: String) {
        viewModelScope.launch {
            modelSettingsDataStore.updateData { settings ->
                settings.toBuilder().setHuggingFaceToken(token).build()
            }
        }
    }

    fun saveNarrativeTone(tone: String) {
        viewModelScope.launch {
            modelSettingsDataStore.updateData { settings ->
                settings.toBuilder().setNarrativeTone(tone).build()
            }
        }
    }

    fun downloadModel(model: DownloadableModel) {
        // TODO: Implementare la logica di download
        Log.d("LlmManagerViewModel", "Download richiesto per: ${model.name}")
        // Per ora, simuliamo il completamento del download salvando un percorso fittizio
        viewModelScope.launch {
            modelSettingsDataStore.updateData { settings ->
                settings.toBuilder().setDmModelFilePath("/path/to/models/${model.id}").build()
            }
        }
    }

    fun deleteDownloadedModel() {
        viewModelScope.launch {
            // TODO: Implementare la logica di cancellazione del file fisico
            Log.d("LlmManagerViewModel", "Cancellazione modello richiesta")
            modelSettingsDataStore.updateData { settings ->
                settings.toBuilder().clearDmModelFilePath().build()
            }
        }
    }
}
