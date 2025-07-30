package io.github.luposolitario.lonewolfredux.ui.screen

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.luposolitario.lonewolfredux.ui.composables.BookWebView
import io.github.luposolitario.lonewolfredux.ui.composables.SaveLoadDialog
import io.github.luposolitario.lonewolfredux.ui.composables.SheetWebView
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel
import org.json.JSONArray
import org.json.JSONObject
// Funzione helper per caricare lo script
private fun getJsFromAssets(context: Context, fileName: String): String {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        Log.e("GameScreen", "Errore durante la lettura del file JS: $fileName", e)
        ""
    }
}

// Interfaccia JS (invariata)
private class GemmaJsInterface(private val viewModel: GameViewModel) {
    @JavascriptInterface
    fun translateParagraphs(paragraphsJson: String) {
        Log.d("GemmaJsInterface", "Ricevuti paragrafi da tradurre: $paragraphsJson")
        val paragraphs = mutableListOf<Pair<String, String>>()
        val jsonArray = JSONArray(paragraphsJson)
        for (i in 0 until jsonArray.length()) {
            val jsonObj = jsonArray.getJSONObject(i)
            paragraphs.add(Pair(jsonObj.getString("id"), jsonObj.getString("html")))
        }
        viewModel.translateParagraphs(paragraphs)
    }
}
// Funzione di controllo URL (invariata)
private fun isStorySection(url: String): Boolean {
    return url.contains(Regex("sect\\d+\\.htm$")) //|| url.endsWith("tssf.htm") || url.endsWith("title.htm")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel,
               onClose: () -> Unit) {

    val isShowingSheet by viewModel.isShowingSheet.collectAsStateWithLifecycle()
    val bookUrl by viewModel.bookUrl.collectAsStateWithLifecycle()
    val sheetUrl by viewModel.sheetUrl.collectAsStateWithLifecycle()
    val jsToRun by viewModel.jsToRunInSheet.collectAsStateWithLifecycle()
    val bookmarkUrl by viewModel.bookmarkUrl.collectAsStateWithLifecycle()
    val isBookCompleted by viewModel.isCurrentBookCompleted.collectAsStateWithLifecycle()
    val showSaveLoadDialog by viewModel.showSaveLoadDialog.collectAsStateWithLifecycle()
    val saveSlots by viewModel.saveSlots.collectAsStateWithLifecycle()
    val jsToRunInBook by viewModel.jsToRunInBook.collectAsStateWithLifecycle()
    val fontZoom by viewModel.fontZoomLevel.collectAsStateWithLifecycle()
    val showZoomSlider by viewModel.showZoomSlider.collectAsStateWithLifecycle()
    val translatedContent by viewModel.translatedContent.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingTranslation.collectAsStateWithLifecycle() // Assicurati di avere questa riga
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val context = LocalContext.current

    val unifiedWebViewClient = remember {
        object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return true
                viewModel.onNewUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val currentUrl = url ?: return
                // 2. Decidiamo QUALE traduttore usare.
                if (isStorySection(currentUrl)) {
                    // --- LOGICA DI INIEZIONE CORRETTA ---
                    // 1. Iniettiamo SEMPRE lo script base quando una pagina finisce di caricare.
                    val gemmaScript = getJsFromAssets(context, "gemma_translator.js")
                    if (gemmaScript.isNotEmpty()) {
                        view?.evaluateJavascript(gemmaScript, null)
                    }
                    Log.d("WebViewClient", "Pagina di storia ($currentUrl). Avvio traduzione Gemma.")
                    // Ora chiamiamo solo la funzione, sicuri che esista.
                    view?.evaluateJavascript("javascript:extractAndTranslateParagraphs();", null)
                }
            }
        }
    }

    // AGGIUNGI QUESTO NUOVO BLOCCO
    LaunchedEffect(isLoading) {
        webViewRef?.post {
            if (isLoading) {
                Log.d("GameScreen", "Stato caricamento: VERO. Mostro la clessidra.")
                webViewRef?.evaluateJavascript("showLoadingIndicator();", null)
            } else {
                Log.d("GameScreen", "Stato caricamento: FALSO. Nascondo la clessidra.")
                webViewRef?.evaluateJavascript("hideLoadingIndicator();", null)
            }
        }
    }

    // SOSTITUISCI IL TUO LaunchedEffect CON QUESTO:
    LaunchedEffect(translatedContent) {
        translatedContent?.let { batch ->
            if (batch.isNotEmpty() && webViewRef != null) {
                val jsonObject = JSONObject(batch as Map<*, *>)

                // === LA VERA CORREZIONE È QUI ===
                // Usiamo il metodo quote() di JSONObject che fa l'escape di TUTTI i caratteri necessari,
                // inclusi \n, \r, \t, e virgolette.
                val properlyEscapedJsonString = jsonObject.toString()
                    .replace("\\", "\\\\") // Escape dei backslash
                    .replace("'", "\\'")   // Escape degli apici singoli
                    .replace("\"", "\\\"") // Escape delle virgolette doppie
                    .replace("\n", "\\n")  // Escape dei "a capo"
                    .replace("\r", "\\r")  // Escape dei "carriage return"

                val script = "replaceBatchParagraphs('${properlyEscapedJsonString}');"
                // =================================

                Log.d("GameScreen", "Invio batch di ${batch.size} traduzioni a JS.")

                webViewRef?.post {
                    webViewRef?.evaluateJavascript(script, null)
                }

                viewModel.consumeTranslatedContent()
            }
        }
    }


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
    Scaffold(topBar = { TopAppBar(title = { Text(if (isShowingSheet) "Scheda Azione" else "") }, navigationIcon = {
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
    }) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            viewModel.openZoomSlider()
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
                    textZoom = fontZoom,
                    onWebViewReady = { webView ->
                        webViewRef = webView
                        webView.addJavascriptInterface(GemmaJsInterface(viewModel), "GemmaTranslator")
                    },
                    webViewClient = unifiedWebViewClient
                )
            }
        }
    }
}
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