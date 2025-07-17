package io.github.luposolitario.lonewolfredux.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.luposolitario.lonewolfredux.ui.composables.BookWebView
import io.github.luposolitario.lonewolfredux.ui.composables.SheetWebView
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel
import androidx.compose.material.icons.filled.* // Per le icone "piene" (Filled)
import androidx.compose.material.icons.outlined.* // Per le icone con solo contorno (Outlined)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel, onClose: () -> Unit) {

    val isShowingSheet by viewModel.isShowingSheet.collectAsState()
    val bookUrl by viewModel.bookUrl.collectAsState()
    val sheetUrl by viewModel.sheetUrl.collectAsState()
    val jsToRun by viewModel.jsToRunInSheet.collectAsState()
    // --- QUESTA È LA RIGA CHIAVE ---
    // Raccogliamo lo stato del segnalibro in una variabile locale.
    // Ogni volta che cambia nel ViewModel, questa parte si aggiornerà.
    val bookmarkUrl by viewModel.bookmarkUrl.collectAsState()
    // --- FINE BLOCCO ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isShowingSheet) "Scheda Azione" else "Libro") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Chiudi") }
                },
                actions = {
                    if (!isShowingSheet) {
                        IconButton(onClick = { viewModel.onHomeClicked() }) {
                            Icon(Icons.Default.Home, "Home")
                        }
                        IconButton(onClick = { viewModel.onBackClicked() }) {
                            Icon(Icons.Default.ArrowBack, "Indietro")
                        }
                        IconButton(onClick = { viewModel.onBookmarkClicked() }) {
                            // Usiamo lo stato raccolto 'bookmarkUrl'
                            val isBookmarked = bookUrl == bookmarkUrl && bookmarkUrl != null
                            Icon(
                                if (isBookmarked) Icons.Filled.Star else Icons.Outlined.Star,
                                "Segnalibro",
                                tint = if (isBookmarked) Color(0xFFFFD700) else LocalContentColor.current
                            )
                        }
                        // Usiamo lo stato raccolto 'bookmarkUrl' per abilitare il pulsante
                        IconButton(onClick = { viewModel.onGoToBookmarkClicked() }, enabled = bookmarkUrl != null) {
                            Icon(Icons.Default.Bookmark, "Vai al Segnalibro")
                        }
                    }
                    IconButton(onClick = { viewModel.toggleSheetVisibility() }) {
                        Icon(
                            imageVector = if (isShowingSheet) Icons.Default.Book else Icons.Default.Person,
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
                SheetWebView(
                    modifier = Modifier.fillMaxSize(),
                    url = sheetUrl,
                    viewModel = viewModel,
                    jsToRun = jsToRun,
                    onJsExecuted = { viewModel.onJsExecuted() }
                )
            } else {
                BookWebView(
                    modifier = Modifier.fillMaxSize(),
                    url = bookUrl,
                    onNewUrl = { viewModel.onNewUrl(it) }
                )
            }
        }
    }
}