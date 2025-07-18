// In: main/java/io/github/luposolitario/lonewolfredux/bridge/TtsInterface.kt

package io.github.luposolitario.lonewolfredux.bridge

import android.webkit.JavascriptInterface
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

/**
 * Interfaccia per permettere al JavaScript di invocare il servizio TTS.
 */
class TtsInterface(private val viewModel: GameViewModel) {

    @JavascriptInterface
    fun speak(text: String) {
        // Inoltra semplicemente la richiesta al ViewModel
        viewModel.speakText(text)
    }
}