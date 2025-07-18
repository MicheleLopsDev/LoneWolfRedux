package io.github.luposolitario.lonewolfredux.ui.screen

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility // Ancora necessaria se vuoi altre animazioni, ma non per l'indicatore di zoom
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures // Importa questo!
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.luposolitario.lonewolfredux.ui.composables.BookWebView
import io.github.luposolitario.lonewolfredux.ui.composables.SaveLoadDialog
import io.github.luposolitario.lonewolfredux.ui.composables.SheetWebView
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ZoomSliderPanel(
    currentZoom: Int,
    onZoomChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dimensione Testo (${currentZoom}%)") },
        text = {
            Slider(
                value = currentZoom.toFloat(),
                onValueChange = { newValue -> onZoomChange(newValue.toInt()) },
                valueRange = 75f..200f,
                steps = 24
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel, onClose: () -> Unit) {

    val isShowingSheet by viewModel.isShowingSheet.collectAsState()
    val bookUrl by viewModel.bookUrl.collectAsState()
    val sheetUrl by viewModel.sheetUrl.collectAsState()
    val jsToRun by viewModel.jsToRunInSheet.collectAsState()
    val bookmarkUrl by viewModel.bookmarkUrl.collectAsState()
    val isBookCompleted by viewModel.isCurrentBookCompleted.collectAsState()
    val showSaveLoadDialog by viewModel.showSaveLoadDialog.collectAsState()
    val saveSlots by viewModel.saveSlots.collectAsState()
    val jsToRunInBook by viewModel.jsToRunInBook.collectAsState()
    val fontZoom by viewModel.fontZoomLevel.collectAsState()
    val showZoomSlider by viewModel.showZoomSlider.collectAsState()
    var webViewRef by remember { mutableStateOf<WebView?>(null) } // Questa reference è cruciale

    if (showZoomSlider) {
        ZoomSliderPanel(
            currentZoom = fontZoom,
            onZoomChange = { viewModel.onZoomChange(it) },
            onDismiss = { viewModel.closeZoomSlider() }
        )
    }

    if (showSaveLoadDialog) {
        SaveLoadDialog(
            slots = saveSlots,
            onDismiss = { viewModel.closeSaveLoadDialog() },
            onSave = { slotId -> viewModel.saveGame(slotId) },
            onLoad = { slotId -> viewModel.loadGame(slotId) },
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isShowingSheet) "Scheda Azione" else "") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Chiudi") }
                },
                actions = {
                    if (isShowingSheet) {
                        IconButton(onClick = { viewModel.openSaveLoadDialog() }) {
                            Icon(Icons.Default.Save, contentDescription = "Salva o Carica")
                        }
                    } else {
                        IconButton(onClick = { viewModel.toggleBookCompletion() }) {
                            Icon(
                                imageVector = if (isBookCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = "Segna come completato",
                                tint = if (isBookCompleted) Color(0xFF4CAF50) else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = {
                            webViewRef?.evaluateJavascript("getVisibleText()") { fullText ->
                                val cleanText = fullText.removeSurrounding("\"")
                                if (cleanText.isNotBlank()) {
                                    viewModel.speakText(cleanText)
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Leggi pagina intera"
                            )
                        }
                        IconButton(onClick = { viewModel.onHomeClicked() }) {
                            Icon(Icons.Default.Home, "Home")
                        }
                        IconButton(onClick = { viewModel.onBackClicked() }) {
                            Icon(Icons.Default.ArrowBack, "Indietro")
                        }
                        IconButton(onClick = { viewModel.onBookmarkClicked() }) {
                            val isBookmarked = bookmarkUrl != null
                            Icon(
                                if (isBookmarked) Icons.Filled.Star else Icons.Outlined.Star,
                                "Segnalibro",
                                tint = if (isBookmarked) Color(0xFFFFD700) else LocalContentColor.current
                            )
                        }
                        IconButton(
                            onClick = { viewModel.onGoToBookmarkClicked() },
                            enabled = bookmarkUrl != null
                        ) {
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
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                // Rimosso .pointerInput per i gesti di zoom
                .pointerInput(Unit) { // Nuovo blocco per il doppio tap
                    detectTapGestures(
                        onDoubleTap = {
                            viewModel.openZoomSlider() // Apri lo slider con doppio tap
                        }
                    )
                }
        ) {
            if (isShowingSheet) {
                SheetWebView(
                    modifier = Modifier.fillMaxSize(),
                    url = sheetUrl,
                    viewModel = viewModel,
                    jsToRun = jsToRun,
                    onJsExecuted = { viewModel.onJsExecuted() },
                    textZoom = fontZoom
                )
            } else {
                BookWebView(
                    modifier = Modifier.fillMaxSize(),
                    url = bookUrl,
                    viewModel = viewModel,
                    jsToRun = jsToRunInBook,
                    onJsExecuted = { viewModel.onBookJsExecuted() },
                    onNewUrl = { viewModel.onNewUrl(it) },
                    textZoom = fontZoom,
                    onWebViewReady = { webView -> webViewRef = webView }
                )
            }

            // Rimosso l'indicatore di zoom gestuale
        }
    }
}