package io.github.luposolitario.lonewolfredux.ui.composables

import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.luposolitario.lonewolfredux.bridge.SheetInterface
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

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
fun BookWebView(modifier: Modifier, url: String, onNewUrl: (String) -> Unit) {
    AndroidView(
        modifier = modifier,
        factory = {
            WebView(it).apply {
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        onNewUrl(url)
                        return true
                    }
                }
            }
        },
        update = {
            if (it.url != url) {
                it.loadUrl(url)
            }
        }
    )
}

@Composable
fun SheetWebView(modifier: Modifier, url: String, viewModel: GameViewModel, jsToRun: String?, onJsExecuted: () -> Unit) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                addJavascriptInterface(SheetInterface(viewModel), "Android")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Carica ed esegue lo script di override dal file assets.
                        val overrideScript = getJsFromAssets(context, "override.js")
                        if (overrideScript.isNotEmpty()) {
                            view?.evaluateJavascript(overrideScript, null)
                        }
                    }
                }
            }
        },
        update = { webView ->
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