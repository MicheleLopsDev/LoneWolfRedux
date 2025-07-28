package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.luposolitario.lonewolfredux.datastore.ModelSettingsManager

class LlmManagerViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LlmManagerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LlmManagerViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}