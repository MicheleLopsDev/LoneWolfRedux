package io.github.luposolitario.lonewolfredux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.luposolitario.lonewolfredux.engine.GemmaTranslationEngine
import io.github.luposolitario.lonewolfredux.ui.screen.GameScreen
import io.github.luposolitario.lonewolfredux.ui.theme.LoneWolfReduxTheme
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

class GameActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val gemmaEngine = GemmaTranslationEngine(application)
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(application, gemmaEngine) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bookId = intent.getIntExtra("BOOK_ID", -1)
        if (bookId == -1) {
            finish()
            return
        }

        viewModel.initialize(bookId)

        setContent {
            LoneWolfReduxTheme {
                // La GameActivity ora ha solo il compito di ospitare lo schermo.
                // Tutta la logica complessa è stata spostata dentro GameScreen.
                GameScreen(viewModel = viewModel) {
                    finish()
                }
            }
        }
    }
}