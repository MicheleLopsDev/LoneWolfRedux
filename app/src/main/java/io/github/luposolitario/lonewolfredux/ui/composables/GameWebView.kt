package io.github.luposolitario.lonewolfredux.ui.composables

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.luposolitario.lonewolfredux.bridge.SheetInterface
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

private fun getJsFromAssets(context: Context, fileName: String): String {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: Exception) { "" }
}

@Composable
fun BookWebView(modifier: Modifier, url: String, onNewUrl: (String) -> Unit) {
    AndroidView(
        modifier = modifier,
        factory = {
            WebView(it).apply {
                // --- INIZIO BLOCCO DI MODIFICA ---
                settings.javaScriptEnabled = true
                // Concediamo alla WebView il permesso di accedere ai file locali
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                // --- FINE BLOCCO DI MODIFICA ---

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        onNewUrl(url)
                        return true
                    }
                }
            }
        },
        update = { it.loadUrl(url) }
    )
}

@Composable
fun SheetWebView(modifier: Modifier, url: String, viewModel: GameViewModel, jsToRun: String?, onJsExecuted: () -> Unit) {
    lateinit var webView: WebView
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webView = this
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                addJavascriptInterface(SheetInterface(viewModel), "Android")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val script = getJsFromAssets(context, "override.js")
                        view?.evaluateJavascript(script, null)
                    }
                }
            }
        },
        update = {
            if (it.url != url) it.loadUrl(url)
            jsToRun?.let { script ->
                it.evaluateJavascript(script, null)
                onJsExecuted()
            }
        }
    )
}