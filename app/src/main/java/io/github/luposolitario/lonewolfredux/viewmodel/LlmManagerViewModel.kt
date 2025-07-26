package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.luposolitario.lonewolfredux.datastore.SettingsDataStoreManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LlmManagerViewModel(
    application: Application,
    private val settingsDataStoreManager: SettingsDataStoreManager
) : AndroidViewModel(application) {

    val uiState: StateFlow<ModelSettingsUiState> = settingsDataStoreManager.modelSettingsFlow
        .map { settings ->
            ModelSettingsUiState(
                huggingFaceToken = settings.huggingFaceToken,
                temperature = settings.gemmaTemperature.ifEmpty { "0.9" },
                topK = settings.gemmaTopK.ifEmpty { "50" },
                topP = settings.gemmaTopP.ifEmpty { "1.0" },
                maxLength = settings.gemmaNLen.ifEmpty { "2048" }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ModelSettingsUiState()
        )

    fun onTokenChanged(newToken: String) {
        viewModelScope.launch {
            settingsDataStoreManager.updateHuggingFaceToken(newToken)
        }
    }

    fun onTemperatureChanged(newTemp: String) {
        viewModelScope.launch {
            settingsDataStoreManager.updateGemmaTemperature(newTemp)
        }
    }

    fun onTopKChanged(newTopK: String) {
        viewModelScope.launch {
            settingsDataStoreManager.updateGemmaTopK(newTopK)
        }
    }

    fun onTopPChanged(newTopP: String) {
        viewModelScope.launch {
            settingsDataStoreManager.updateGemmaTopP(newTopP)
        }
    }

    fun onMaxLengthChanged(newLength: String) {
        viewModelScope.launch {
            settingsDataStoreManager.updateGemmaNLen(newLength)
        }
    }
}

data class ModelSettingsUiState(
    val huggingFaceToken: String = "",
    val temperature: String = "0.9",
    val topK: String = "50",
    val topP: String = "1.0",
    val maxLength: String = "2048"
)