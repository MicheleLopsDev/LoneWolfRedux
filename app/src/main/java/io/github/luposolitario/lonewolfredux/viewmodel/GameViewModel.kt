package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

// MODIFICA 1: La classe ora estende AndroidViewModel
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _bookUrl = MutableStateFlow("about:blank")
    val bookUrl = _bookUrl.asStateFlow()

    private val _sheetUrl = MutableStateFlow("about:blank")
    val sheetUrl = _sheetUrl.asStateFlow()

    private val _jsToRunInSheet = MutableStateFlow<String?>(null)
    val jsToRunInSheet = _jsToRunInSheet.asStateFlow()

    fun initialize(bookId: Int) {
        val context = getApplication<Application>().applicationContext

        val bookFile = File(context.filesDir, "books/$bookId/title.htm")

        // MODIFICA 2: Costruiamo l'URL manualmente
        val bookPath = "file://${bookFile.absolutePath}"

        _bookUrl.value = bookPath

        _sheetUrl.value = when (bookId) {
            in 1..5 -> "file:///android_asset/sheets/char_sheet_301.htm"
            in 6..12 -> "file:///android_asset/sheets/char_sheet_302.htm"
            in 13..20 -> "file:///android_asset/sheets/char_sheet_303.htm"
            else -> "file:///android_asset/sheets/char_sheet_304.htm"
        }
    }

    fun onNewUrl(url: String) {
        _bookUrl.value = url
    }

    fun runJsInSheetView(script: String) {
        _jsToRunInSheet.value = script
    }

    fun onJsExecuted() {
        _jsToRunInSheet.value = null
    }
}