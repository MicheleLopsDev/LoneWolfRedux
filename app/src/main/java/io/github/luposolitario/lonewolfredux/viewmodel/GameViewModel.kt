package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import io.github.luposolitario.lonewolfredux.datastore.AppSettingsManager
import io.github.luposolitario.lonewolfredux.datastore.GameSession
import io.github.luposolitario.lonewolfredux.datastore.SaveGameManager
import io.github.luposolitario.lonewolfredux.datastore.SaveSlotInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class GameViewModel(application: Application) : AndroidViewModel(application) {

    // --- STATO INTERNO DEL VIEWMODEL ---
    private var activeSlotId: Int = 1
    // Deve essere inizializzato correttamente da `initialize`
    private var currentBookId: Int = -1

    // (Il resto delle dichiarazioni StateFlow rimane invariato...)
    private val _bookUrl = MutableStateFlow("about:blank")
    val bookUrl: StateFlow<String> = _bookUrl.asStateFlow()
    private val _sheetUrl = MutableStateFlow("about:blank")
    val sheetUrl: StateFlow<String> = _sheetUrl.asStateFlow()
    private val _jsToRunInSheet = MutableStateFlow<String?>(null)
    val jsToRunInSheet: StateFlow<String?> = _jsToRunInSheet.asStateFlow()
    private val _isShowingSheet = MutableStateFlow(false)
    val isShowingSheet: StateFlow<Boolean> = _isShowingSheet.asStateFlow()
    private val _navigationHistory = MutableStateFlow<List<String>>(emptyList())
    private val _bookmarkUrl = MutableStateFlow<String?>(null)
    val bookmarkUrl: StateFlow<String?> = _bookmarkUrl.asStateFlow()
    private val _saveSlots = MutableStateFlow<List<SaveSlotInfo>>(emptyList())
    val saveSlots: StateFlow<List<SaveSlotInfo>> = _saveSlots.asStateFlow()
    private val _showSaveLoadDialog = MutableStateFlow(false)
    val showSaveLoadDialog: StateFlow<Boolean> = _showSaveLoadDialog.asStateFlow()

    private val _isCurrentBookCompleted = MutableStateFlow(false)
    val isCurrentBookCompleted: StateFlow<Boolean> = _isCurrentBookCompleted.asStateFlow()


    // --- FUNZIONI DI INIZIALIZZAZIONE E GESTIONE DEL GIOCO ---
    fun initialize(bookId: Int) {
        if (bookId <= 0) {
            Log.e("GameViewModel", "ID del libro non valido: $bookId. Impossibile inizializzare.")
            return
        }
        currentBookId = bookId
        // Aggiorna la logica per usare il nuovo manager
        viewModelScope.launch {
            _isCurrentBookCompleted.value = AppSettingsManager.isBookCompleted(getApplication(), currentBookId)
        }

        loadGame(1) // Carica lo slot 1 del libro corrente
    }

    fun toggleBookCompletion() {
        if (currentBookId > 0) {
            viewModelScope.launch {
                AppSettingsManager.toggleBookCompletion(getApplication(), currentBookId)
                _isCurrentBookCompleted.value = AppSettingsManager.isBookCompleted(getApplication(), currentBookId)
            }
        }
    }


    fun loadGame(slotId: Int) {
        viewModelScope.launch {
            activeSlotId = slotId
            // Passa il bookId corrente al SaveGameManager
            val session = SaveGameManager.getSession(getApplication(), currentBookId, activeSlotId)

            _navigationHistory.value = session.navigationHistoryList
            _bookmarkUrl.value = session.bookmarkedParagraphUrl.ifEmpty { null }

            val urlToLoad = if (session.navigationHistoryList.isEmpty()) {
                val bookFile = File(getApplication<Application>().filesDir, "books/$currentBookId/title.htm")
                "file://${bookFile.absolutePath}"
            } else {
                session.navigationHistoryList.last()
            }
            _bookUrl.value = urlToLoad
            if (session.navigationHistoryList.isEmpty()) {
                addUrlToHistory(urlToLoad)
            }

            _sheetUrl.value = when (currentBookId) {
                in 1..5 -> "file:///android_asset/sheets/char_sheet_301.htm"
                in 6..12 -> "file:///android_asset/sheets/char_sheet_302.htm"
                in 13..20 -> "file:///android_asset/sheets/char_sheet_303.htm"
                else -> "file:///android_asset/sheets/char_sheet_304.htm"
            }
            closeSaveLoadDialog()
            runJsInSheetView("loadAllData();")
        }
    }

    fun onSheetDataExtracted(jsonData: String) {
        Log.d("GameViewModel", "Dati estratti per il salvataggio nel libro $currentBookId, slot $activeSlotId")
        viewModelScope.launch {
            val dataMap: Map<String, String> = Gson().fromJson(jsonData, object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type)
            val sessionToSave = GameSession.newBuilder()
                .putAllSheetData(dataMap)
                .clearNavigationHistory()
                .addAllNavigationHistory(_navigationHistory.value)
                .setBookmarkedParagraphUrl(_bookmarkUrl.value ?: "")
                .build()

            // Passa il bookId corrente al SaveGameManager
            SaveGameManager.updateSession(getApplication(), currentBookId, activeSlotId, sessionToSave)
            Log.d("GameViewModel", "Salvataggio completato")

            refreshSaveSlots()
            closeSaveLoadDialog()
        }
    }

    fun loadSheetDataIntoWebView(callbackId: String) {
        viewModelScope.launch {
            // Passa il bookId corrente al SaveGameManager
            val dataMap = SaveGameManager.getSession(getApplication(), currentBookId, activeSlotId).sheetDataMap
            val jsonData = Gson().toJson(dataMap)
            val escapedJsonData = jsonData.replace("'", "\\'")
            val script = "window.nativeCallback('$callbackId', '$escapedJsonData');"
            runJsInSheetView(script)
        }
    }

    private suspend fun refreshSaveSlots() {
        // Passa il bookId corrente al SaveGameManager
        _saveSlots.value = SaveGameManager.getSaveSlotsInfo(getApplication(), currentBookId)
    }

    // (Il resto del file: saveGame, open/close dialog, funzioni di navigazione, ecc. rimane identico)
    fun saveGame(slotId: Int) {
        viewModelScope.launch {
            activeSlotId = slotId
            val script = """
                (function() {
                    var data = {};
                    var form = document.actionChart;
                    for (var i = 0; i < form.elements.length; i++) {
                        var field = form.elements[i];
                        if (field.name) {
                            data[field.name] = (field.type === 'checkbox') ? String(field.checked) : field.value;
                        }
                    }
                    window.Android.onSheetDataExtracted(JSON.stringify(data));
                })();
            """.trimIndent()
            runJsInSheetView(script)
        }
    }

    fun openSaveLoadDialog() {
        viewModelScope.launch {
            refreshSaveSlots()
            _showSaveLoadDialog.value = true
        }
    }

    fun closeSaveLoadDialog() {
        _showSaveLoadDialog.value = false
    }

    fun onNewUrl(url: String) {
        _bookUrl.value = url
        addUrlToHistory(url)
    }

    private fun addUrlToHistory(url: String) {
        if (_navigationHistory.value.lastOrNull() != url) {
            _navigationHistory.value = _navigationHistory.value + url
        }
    }

    fun onHomeClicked() {
        if (_navigationHistory.value.isNotEmpty()) {
            val homeUrl = _navigationHistory.value.first()
            _bookUrl.value = homeUrl
            _navigationHistory.value = listOf(homeUrl)
        }
    }

    fun onBackClicked() {
        if (_navigationHistory.value.size > 1) {
            val newHistory = _navigationHistory.value.dropLast(1)
            _navigationHistory.value = newHistory
            _bookUrl.value = newHistory.last()
        }
    }

    fun onBookmarkClicked() {
        val currentUrl = _bookUrl.value
        _bookmarkUrl.value = if (_bookmarkUrl.value == currentUrl) null else currentUrl
    }

    fun onGoToBookmarkClicked() {
        _bookmarkUrl.value?.let { bookmarkedUrl ->
            _bookUrl.value = bookmarkedUrl
            addUrlToHistory(bookmarkedUrl)
        }
    }

    fun toggleSheetVisibility() {
        _isShowingSheet.value = !_isShowingSheet.value
    }

    fun runJsInSheetView(script: String) {
        _jsToRunInSheet.value = script
    }

    fun onJsExecuted() {
        _jsToRunInSheet.value = null
    }
}