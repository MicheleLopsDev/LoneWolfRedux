package io.github.luposolitario.lonewolfredux.bridge

import android.util.Log
import android.webkit.JavascriptInterface
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

class SheetInterface(private val viewModel: GameViewModel) {
    // Lasciamo questa funzione vuota per ora, la riempiremo con la nuova logica
    @JavascriptInterface
    fun saveSheetData(jsonData: String) {
        Log.d("GameViewModel", "Dati estratti ricevuti: $jsonData")
        // Logica di salvataggio futura qui...
    }
}