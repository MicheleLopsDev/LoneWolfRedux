package io.github.luposolitario.lonewolfredux.bridge

import android.webkit.JavascriptInterface
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

class SheetInterface(private val viewModel: GameViewModel) {

    // Questa è l'unica funzione che il JS chiamerà
    @JavascriptInterface
    fun onSheetDataExtracted(jsonData: String) {
        viewModel.saveSheetData(jsonData)
    }
}