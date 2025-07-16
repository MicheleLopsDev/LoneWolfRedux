package io.github.luposolitario.lonewolfredux.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.luposolitario.lonewolfredux.ui.composables.BookWebView
import io.github.luposolitario.lonewolfredux.ui.composables.SheetWebView
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel, onClose: () -> Unit) {

    val isShowingSheet by viewModel.isShowingSheet.collectAsState()
    val bookUrl by viewModel.bookUrl.collectAsState()
    val sheetUrl by viewModel.sheetUrl.collectAsState()
    val jsToRun by viewModel.jsToRunInSheet.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isShowingSheet) "Scheda Azione" else "Libro") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "Chiudi")
                    }
                },
                actions = {
                    // L'icona ora è un interruttore (toggle)
                    IconButton(onClick = { viewModel.toggleSheetVisibility() }) {
                        Icon(
                            imageVector = if (isShowingSheet) Icons.Default.AccountBox else Icons.Default.Person,
                            contentDescription = if (isShowingSheet) "Mostra Libro" else "Mostra Scheda Azione"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            if (isShowingSheet) {
                // Se dobbiamo mostrare la scheda
                SheetWebView(
                    modifier = Modifier.fillMaxSize(),
                    url = sheetUrl,
                    viewModel = viewModel,
                    jsToRun = jsToRun,
                    onJsExecuted = { viewModel.onJsExecuted() }
                )
            } else {
                // Altrimenti, mostriamo il libro
                BookWebView(
                    modifier = Modifier.fillMaxSize(),
                    url = bookUrl,
                    onNewUrl = { viewModel.onNewUrl(it) }
                )
            }
        }
    }
}