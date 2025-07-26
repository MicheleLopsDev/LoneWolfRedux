package io.github.luposolitario.lonewolfredux

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.luposolitario.lonewolfredux.navigation.AppNavigator
import io.github.luposolitario.lonewolfredux.ui.theme.LoneWolfReduxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoneWolfReduxTheme {
                MainMenuScreen(
                    onNavigateToLibrary = {
                        // Naviga alla libreria usando il nostro Navigator
                        AppNavigator.navigateToDownloadManager(this)
                    },
                    onNavigateToSettings = {
                        // Avvia la nuova ConfigurationActivity
                        startActivity(Intent(this, ConfigurationActivity::class.java))
                    },
                    // Lasciamo questo vuoto per ora, come da piano
                    onNavigateToLlmManager = {
                        startActivity(Intent(this, LlmManagerActivity::class.java))
                    }
                )
            }
        }
    }
}