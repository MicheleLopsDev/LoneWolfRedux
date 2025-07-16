package io.github.luposolitario.lonewolfredux.bridge

import android.webkit.JavascriptInterface
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel
import kotlinx.coroutines.launch

class SheetInterface(private val viewModel: GameViewModel) {
    @JavascriptInterface
    fun saveData(key: String, value: String) {
        // Per ora, logghiamo. Aggiungeremo il salvataggio in seguito.
        println("SAVE: $key = $value")
    }

    @JavascriptInterface
    fun loadAllChartData(callbackId: String) {
        viewModel.viewModelScope.launch {
            // Per ora, restituiamo dati vuoti.
            val allData: Map<String, String> = emptyMap()
            val jsonString = Gson().toJson(allData).replace("'", "\\'")
            val script = "window.nativeCallback($callbackId, '$jsonString');"
            viewModel.runJsInSheetView(script)
        }
    }
}