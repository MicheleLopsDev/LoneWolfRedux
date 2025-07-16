package io.github.luposolitario.lonewolfredux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import io.github.luposolitario.lonewolfredux.ui.screen.GameScreen
import io.github.luposolitario.lonewolfredux.ui.theme.LoneWolfReduxTheme
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

class GameActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

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