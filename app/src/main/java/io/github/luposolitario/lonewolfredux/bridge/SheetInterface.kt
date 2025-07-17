package io.github.luposolitario.lonewolfredux.bridge

import android.util.Log
import android.webkit.JavascriptInterface
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel

class SheetInterface(private val viewModel: GameViewModel) {

    /**
     * Questo è il metodo che il JavaScript può vedere e chiamare.
     * Deve corrispondere esattamente alla chiamata in GameViewModel (`window.Android.onSheetDataExtracted`).
     */
    @JavascriptInterface
    fun onSheetDataExtracted(jsonData: String) {
        Log.d("SheetInterface", "Dati ricevuti da JS: $jsonData")
        viewModel.onSheetDataExtracted(jsonData)
    }

    /**
     * Chiamato dal JS per richiedere tutti i dati della scheda.
     * @param callbackId Un ID univoco generato dal JS per gestire la risposta asincrona.
     */
    @JavascriptInterface
    fun loadAllSheetData(callbackId: String) {
        Log.d("SheetInterface", "JS richiede i dati della scheda con callbackId: $callbackId")
        viewModel.loadSheetDataIntoWebView(callbackId)
    }
}