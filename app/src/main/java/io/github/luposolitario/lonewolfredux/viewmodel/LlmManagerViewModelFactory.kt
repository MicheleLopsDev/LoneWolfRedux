package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.luposolitario.lonewolfredux.datastore.SettingsDataStoreManager

class LlmManagerViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LlmManagerViewModel::class.java)) {
            val settingsDataStoreManager = SettingsDataStoreManager(application)
            @Suppress("UNCHECKED_CAST")
            return LlmManagerViewModel(application, settingsDataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}