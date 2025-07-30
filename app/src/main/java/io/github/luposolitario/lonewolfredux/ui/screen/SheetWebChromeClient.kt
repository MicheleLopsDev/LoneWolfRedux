package io.github.luposolitario.lonewolfredux.ui.screen

import android.webkit.JsPromptResult // <-- CORREZIONE: Import corretto
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

class SheetWebChromeClient(private val viewModel: GameViewModel) : WebChromeClient() {

    /**
     * Intercetta le chiamate `alert()` dal JavaScript.
     */
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        if (message != null) {
            // Usiamo la nostra funzione per mostrare un dialogo nativo
            viewModel.showSheetDialog("Informazione", message)
        }
        // Diciamo al sistema che abbiamo gestito noi l'alert
        result?.confirm()
        return true
    }

    /**
     * --- FUNZIONE MODIFICATA ---
     * Intercetta `confirm()` e mostra un dialogo di conferma con scelta.
     */
    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        if (message != null && result != null) {
            // Chiama la NUOVA funzione per il dialogo di conferma
            viewModel.showSheetConfirmDialog("Conferma", message, result)
        } else {
            // Fallback nel caso qualcosa vada storto
            result?.cancel()
        }
        return true
    }


    /**
     * Intercetta le chiamate `prompt()` dal JavaScript.
     * Al momento, la gestiamo come un alert.
     */
    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult? // <-- CORREZIONE: Il tipo corretto è JsPromptResult
    ): Boolean {
        if (message != null) {
            viewModel.showSheetDialog("Richiesta", message)
        }
        result?.confirm()
        return true
    }
    // --- FINE CORREZIONE ---
}