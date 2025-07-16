package io.github.luposolitario.lonewolfredux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import io.github.luposolitario.lonewolfredux.ui.screen.DownloadManagerScreen
import io.github.luposolitario.lonewolfredux.ui.theme.LoneWolfReduxTheme
import io.github.luposolitario.lonewolfredux.viewmodel.DownloadManagerViewModel

class DownloadManagerActivity : ComponentActivity() {

    private val viewModel: DownloadManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoneWolfReduxTheme {
                DownloadManagerScreen(viewModel = viewModel)
            }
        }
    }
}