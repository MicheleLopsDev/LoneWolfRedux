package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.luposolitario.lonewolfredux.datastore.AppSettingsManager
import io.github.luposolitario.lonewolfredux.datastore.SaveGameManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConfigurationViewModel(application: Application) : AndroidViewModel(application) {

    private val saveGameManager = SaveGameManager

    // StateFlow per controllare la visibilità del dialogo di conferma
    private val _showResetConfirmationDialog = MutableStateFlow(false)
    val showResetConfirmationDialog: StateFlow<Boolean> = _showResetConfirmationDialog.asStateFlow()


    // In ConfigurationViewModel.kt

    // Mappa delle lingue supportate (codice -> nome visualizzato)
    val availableLanguages = mapOf(
        "en" to "Inglese (Nessuna Traduzione)",
        "it" to "Italiano",
        "fr" to "Francese",
        "es" to "Spagnolo",
        "de" to "Tedesco"
    )

    val targetLanguage: StateFlow<String> = AppSettingsManager.getTargetLanguageFlow(getApplication())
        .stateIn(viewModelScope, SharingStarted.Lazily, "it")

    fun setTargetLanguage(languageCode: String) {
        viewModelScope.launch {
            AppSettingsManager.setTargetLanguage(getApplication(), languageCode)
        }
    }

    fun onResetTotalClicked() {
        _showResetConfirmationDialog.value = true
    }

    fun onResetTotalConfirmed() {
        viewModelScope.launch {
            saveGameManager.deleteAllSaveFiles(getApplication())
            _showResetConfirmationDialog.value = false
            // Qui potresti inviare un evento alla UI per mostrare un Toast di conferma
        }
    }

    fun onResetTotalCancelled() {
        _showResetConfirmationDialog.value = false
    }


    val isAdvancedTranslationEnabled: StateFlow<Boolean> = flow {
        emit(AppSettingsManager.isAdvancedTranslationEnabled(getApplication()))
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setUseAdvancedTranslation(enabled: Boolean) {
        viewModelScope.launch {
            AppSettingsManager.setUseAdvancedTranslation(getApplication(), enabled)
        }
    }

}