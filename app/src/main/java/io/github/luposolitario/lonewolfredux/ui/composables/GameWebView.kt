package io.github.luposolitario.lonewolfredux.ui.composables

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.luposolitario.lonewolfredux.bridge.SheetInterface
import io.github.luposolitario.lonewolfredux.bridge.TranslationInterface
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel
import io.github.luposolitario.lonewolfredux.bridge.TtsInterface

private fun getJsFromAssets(context: Context, fileName: String): String {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        Log.e("GameWebView", "Errore durante la lettura del file JS dagli assets: $fileName", e)
        ""
    }
}

class WebViewTapInterface(private val onDoubleTap: () -> Unit) {
    @JavascriptInterface
    @Suppress("unused")
    fun onDoubleTapDetected() {
        onDoubleTap()
    }
}

@Composable
fun BookWebView(
    modifier: Modifier,
    url: String,
    viewModel: GameViewModel,
    jsToRun: String?,
    onJsExecuted: () -> Unit,
    textZoom: Int,
    onWebViewReady: (WebView) -> Unit,
    // --- MODIFICA CHIAVE: Accettiamo SOLO il client dall'esterno ---
    webViewClient: WebViewClient
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.textZoom = textZoom
                addJavascriptInterface(TranslationInterface(viewModel), "Translator")
                addJavascriptInterface(WebViewTapInterface { viewModel.openZoomSlider() }, "AndroidTap")
                addJavascriptInterface(TtsInterface(viewModel), "TtsHandler")

                // --- USA IL CLIENT FORNITO. NESSUN ALTRO CLIENT VIENE CREATO ---
                setWebViewClient(webViewClient)

                onWebViewReady(this)
            }
        },
        update = { webView ->
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

@Composable
fun SheetWebView(
    modifier: Modifier,
    url: String,
    viewModel: GameViewModel,
    jsToRun: String?,
    onJsExecuted: () -> Unit,
    textZoom: Int,
    onWebViewReady: (WebView) -> Unit,
    webChromeClient: WebChromeClient
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.textZoom = textZoom

                addJavascriptInterface(SheetInterface(viewModel), "Android")
                addJavascriptInterface(WebViewTapInterface { viewModel.openZoomSlider() }, "AndroidTap")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
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
                this.webViewClient = WebViewClient() // Il client per la navigazione
                this.webChromeClient = webChromeClient // Il NUOVO client per i dialoghi JS
                onWebViewReady(this)
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