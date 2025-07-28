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

    // --- MODIFICA QUESTA RIGA ---
    private val viewModel: GameViewModel by viewModels {
        // Creiamo una Factory "al volo" qui dentro
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val gemmaEngine = GemmaTranslationEngine(application)
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(application, gemmaEngine) as T
            }
        }
    }
    // --- FINE MODIFICA ---

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
                GameScreen(viewModel = viewModel) {
                    finish()
                }
            }
        }
    }
}