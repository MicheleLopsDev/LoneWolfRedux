package io.github.luposolitario.lonewolfredux.ui.composables

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.luposolitario.lonewolfredux.bridge.SheetInterface
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

// Funzione helper getJsFromAssets (invariata)
private fun getJsFromAssets(context: Context, fileName: String): String {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: Exception) { "" }
}

// BookWebView (invariata)
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
            // Per evitare ricaricamenti non necessari
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
                        // CORREZIONE: Iniettiamo lo script per l'override e il caricamento
                        val overrideScript = """
                            // Sistema di callback asincrono
                            window.nativeCallbacks = {};
                            window.nativeCallbackId = 0;
                            function callNative(funcName, ...args) {
                                return new Promise((resolve) => {
                                    const callbackId = window.nativeCallbackId++;
                                    window.nativeCallbacks[callbackId] = resolve;
                                    if (window.Android && typeof window.Android[funcName] === 'function') {
                                        window.Android[funcName](String(callbackId), ...args);
                                    } else {
                                        console.error("Funzione nativa non trovata: " + funcName);
                                        resolve(null);
                                    }
                                });
                            }
                            window.nativeCallback = (callbackId, result) => {
                                if (window.nativeCallbacks[callbackId]) {
                                    window.nativeCallbacks[callbackId](result);
                                    delete window.nativeCallbacks[callbackId];
                                }
                            };

                            // Override della funzione di caricamento originale
                            function loadAllData() {
                                console.log("JS: Richiesta dati al codice nativo...");
                                callNative('loadAllSheetData').then(jsonData => {
                                    if (!jsonData || jsonData === '{}') return;
                                    console.log("JS: Dati ricevuti, popolo la scheda.");
                                    const data = JSON.parse(jsonData);
                                    const form = document.actionChart;
                                    if (!form) return;
                                    for(let i=0; i < form.elements.length; i++) {
                                        const field = form.elements[i];
                                        const savedValue = data[field.name];
                                        if (savedValue !== undefined) {
                                            if (field.type == 'checkbox') {
                                                field.checked = (savedValue === 'true');
                                            } else {
                                                field.value = savedValue;
                                            }
                                        }
                                    }
                                    if (typeof findPercentage === 'function') {
                                        findPercentage();
                                    }
                                });
                            };

                            // Avvia il processo
                            loadAllData();
                        """.trimIndent()
                        view?.evaluateJavascript(overrideScript, null)
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