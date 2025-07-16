package io.github.luposolitario.lonewolfredux.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import io.github.luposolitario.lonewolfredux.navigation.AppNavigator

class MainViewModel : ViewModel() {

    fun onLibraryClicked(context: Context) {
        AppNavigator.navigateToDownloadManager(context)
    }

    fun onLlmManagerClicked(context: Context) {
        AppNavigator.navigateToLlmManager(context)
    }

    fun onConfigurationClicked(context: Context) {
        AppNavigator.navigateToConfiguration(context)
    }
}