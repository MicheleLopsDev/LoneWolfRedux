package io.github.luposolitario.lonewolfredux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import io.github.luposolitario.lonewolfredux.ui.screen.LlmManagerScreen
import io.github.luposolitario.lonewolfredux.ui.theme.LoneWolfReduxTheme
import io.github.luposolitario.lonewolfredux.viewmodel.LlmManagerViewModel
import io.github.luposolitario.lonewolfredux.viewmodel.LlmManagerViewModelFactory

class LlmManagerActivity : ComponentActivity() {

    private val viewModel: LlmManagerViewModel by viewModels {
        LlmManagerViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoneWolfReduxTheme {
                LlmManagerScreen(viewModel = viewModel)
            }
        }
    }
}