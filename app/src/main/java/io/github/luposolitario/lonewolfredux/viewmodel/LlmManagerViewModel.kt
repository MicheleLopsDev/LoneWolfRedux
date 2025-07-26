package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
}
