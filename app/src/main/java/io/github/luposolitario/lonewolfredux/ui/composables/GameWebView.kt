package io.github.luposolitario.lonewolfredux.ui.composables

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.luposolitario.lonewolfredux.bridge.SheetInterface
import io.github.luposolitario.lonewolfredux.bridge.TranslationInterface
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel
import io.github.luposolitario.lonewolfredux.bridge.TtsInterface
/**
 * Legge il contenuto di un file dalla cartella assets e lo restituisce come stringa.
 */
private fun getJsFromAssets(context: Context, fileName: String): String {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        Log.e("GameWebView", "Errore durante la lettura del file JS dagli assets: $fileName", e)
        "" // Restituisce una stringa vuota in caso di errore
    }
}

@Composable
fun BookWebView(
    modifier: Modifier,
    url: String,
    viewModel: GameViewModel,
    jsToRun: String?,
    onJsExecuted: () -> Unit,
    onNewUrl: (String) -> Unit,
    textZoom: Int,
    onWebViewReady: (WebView) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.textZoom = textZoom // <-- APPLICA LO ZOOM
                // Aggiungiamo le nostre interfacce
                addJavascriptInterface(TranslationInterface(viewModel), "Translator")
                addJavascriptInterface(WebViewTapInterface { viewModel.openZoomSlider() }, "AndroidTap")
                addJavascriptInterface(TtsInterface(viewModel), "TtsHandler") // <-- NUOVA INTERFACCIA

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        onNewUrl(url)
                        return true
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Iniettiamo entrambi gli script di override
                        val translatorScript = getJsFromAssets(context, "translator.js")
                        view?.evaluateJavascript(translatorScript, null)

                        val ttsScript = getJsFromAssets(context, "tts_handler.js") // <-- NUOVO SCRIPT
                        view?.evaluateJavascript(ttsScript, null)

                        // --- NUOVO: Inietta lo script per estrarre il testo ---
                        val getTextScript = getJsFromAssets(context, "get_text.js")
                        view?.evaluateJavascript(getTextScript, null)

                        val doubleTapScript = getJsFromAssets(context, "double_tap_detector.js")
                        if (doubleTapScript.isNotEmpty()) {
                            view?.evaluateJavascript(doubleTapScript, null)
                        }
                    }
                }
                onWebViewReady(this)
            }
        },
        update = { webView ->
            // La logica di update rimane la stessa, ma dobbiamo applicare lo zoom
            if (webView.settings.textZoom != textZoom) {
                webView.settings.textZoom = textZoom
            }
            if (webView.url != url) {
                webView.loadUrl(url)
            }
            jsToRun?.let {
                webView.evaluateJavascript(it, null)
                onJsExecuted()
            }
        }
    )
}


class WebViewTapInterface(private val onDoubleTap: () -> Unit) {
    @JavascriptInterface
    @Suppress("unused")
    fun onDoubleTapDetected() {
        // Esegui sul thread principale di Compose
        // Oltre a questo, potresti voler usare un Handler o un CoroutineScope
        // per assicurarti che la chiamata avvenga sul thread UI.
        // Per semplicità, inizialmente chiamiamo direttamente.
        // Se riscontri problemi, potresti dover usare un Handler.
        onDoubleTap()
    }
}

@Composable
fun SheetWebView(
    modifier: Modifier,
    url: String,
    viewModel: GameViewModel,
    jsToRun: String?,
    onJsExecuted: () -> Unit,
    textZoom: Int // <-- NUOVO
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.textZoom = textZoom // <-- APPLICA LO ZOOM

                addJavascriptInterface(SheetInterface(viewModel), "Android")
                addJavascriptInterface(WebViewTapInterface { viewModel.openZoomSlider() }, "AndroidTap")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Carica ed esegue lo script di override dal file assets.
                        val overrideScript = getJsFromAssets(context, "override.js")
                        if (overrideScript.isNotEmpty()) {
                            view?.evaluateJavascript(overrideScript, null)
                        }
                        val doubleTapScript = getJsFromAssets(context, "double_tap_detector.js")
                        if (doubleTapScript.isNotEmpty()) {
                            view?.evaluateJavascript(doubleTapScript, null)
                        }
                    }
                }
            }
        },
        update = { webView ->
            if (webView.settings.textZoom != textZoom) {
                webView.settings.textZoom = textZoom
            }
            if (webView.url != url) {
                webView.loadUrl(url)
            }
            jsToRun?.let { script ->
                webView.evaluateJavascript(script, null)
                onJsExecuted()
            }
        }
    )
}