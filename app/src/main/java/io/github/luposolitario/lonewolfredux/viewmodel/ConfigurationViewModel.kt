package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.luposolitario.lonewolfredux.data.NarrativeTone
import io.github.luposolitario.lonewolfredux.datastore.AppSettings
import io.github.luposolitario.lonewolfredux.datastore.AppSettingsManager
import io.github.luposolitario.lonewolfredux.datastore.SaveGameManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConfigurationViewModel(application: Application) : AndroidViewModel(application) {

    private val saveGameManager = SaveGameManager

    // StateFlow per controllare la visibilità del dialogo di conferma
    private val _showResetConfirmationDialog = MutableStateFlow(false)
    val showResetConfirmationDialog: StateFlow<Boolean> = _showResetConfirmationDialog.asStateFlow()

    val fontZoomLevel: StateFlow<Int> = AppSettingsManager.getFontZoomLevelFlow(getApplication())
        .stateIn(viewModelScope, SharingStarted.Lazily, 100)

    fun setFontZoomLevel(zoomLevel: Int) {
        viewModelScope.launch {
            AppSettingsManager.setFontZoomLevel(getApplication(), zoomLevel)
        }
    }

    // Espone il tono narrativo attualmente salvato come StateFlow
    val selectedNarrativeTone: StateFlow<NarrativeTone> = AppSettingsManager.getNarrativeToneFlow(getApplication())
        .map { toneKey -> NarrativeTone.fromKey(toneKey) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NarrativeTone.Neutro)

    // Espone la lista di tutti i toni disponibili
    val availableTones: List<NarrativeTone> = NarrativeTone.allTones

    /**
     * Chiamato dalla UI quando l'utente seleziona un nuovo tono.
     */
    fun onNarrativeToneSelected(tone: NarrativeTone) {
        viewModelScope.launch {
            // Chiamiamo il metodo sull'object passando il contesto
            AppSettingsManager.setNarrativeTone(getApplication(), tone.key)
        }
    }

    // --- FINE BLOCCO NUOVO ---
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

    // Esponi l'intero oggetto delle impostazioni
    val appSettings: StateFlow<AppSettings> = AppSettingsManager.getTtsSettingsFlow(getApplication())
        .stateIn(viewModelScope, SharingStarted.Lazily, AppSettings.getDefaultInstance())

    // Funzioni specifiche per aggiornare le singole preferenze
    fun setSpeechRate(rate: Float) {
        viewModelScope.launch { AppSettingsManager.updateTtsSettings(getApplication(), rate = rate) }
    }
    fun setPitch(pitch: Float) {
        viewModelScope.launch { AppSettingsManager.updateTtsSettings(getApplication(), pitch = pitch) }
    }

    fun setNarratorVoice(name: String) {
        viewModelScope.launch {
            AppSettingsManager.updateTtsSettings(getApplication(), narratorVoice = name)
        }
    }

}