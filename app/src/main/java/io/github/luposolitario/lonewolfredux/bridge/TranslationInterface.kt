package io.github.luposolitario.lonewolfredux.bridge

import android.webkit.JavascriptInterface
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

class TranslationInterface(private val viewModel: GameViewModel) {

    /**
     * Metodo esposto al JavaScript. Riceve il testo da tradurre e un ID per la callback.
     */
    @JavascriptInterface
    fun translate(text: String, callbackId: Int) {
        viewModel.onTranslateRequest(text, callbackId)
    }
}