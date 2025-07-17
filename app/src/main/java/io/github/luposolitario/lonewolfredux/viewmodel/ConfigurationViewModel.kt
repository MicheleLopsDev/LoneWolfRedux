package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.luposolitario.lonewolfredux.datastore.SaveGameManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfigurationViewModel(application: Application) : AndroidViewModel(application) {

    private val saveGameManager = SaveGameManager

    // StateFlow per controllare la visibilità del dialogo di conferma
    private val _showResetConfirmationDialog = MutableStateFlow(false)
    val showResetConfirmationDialog: StateFlow<Boolean> = _showResetConfirmationDialog.asStateFlow()

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
}