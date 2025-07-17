package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import io.github.luposolitario.lonewolfredux.bridge.SheetInterface // Aggiungi questo import
import io.github.luposolitario.lonewolfredux.datastore.GameSession
import io.github.luposolitario.lonewolfredux.datastore.GameSessionSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File


// Definiamo il DataStore come estensione del Context
private val Context.gameSessionDataStore: DataStore<GameSession> by dataStore(
    fileName = "game_session.pb",
    serializer = GameSessionSerializer
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.gameSessionDataStore

    private val _bookUrl = MutableStateFlow("about:blank")
    val bookUrl = _bookUrl.asStateFlow()

    private val _sheetUrl = MutableStateFlow("about:blank")
    val sheetUrl = _sheetUrl.asStateFlow()

    private val _jsToRunInSheet = MutableStateFlow<String?>(null)
    val jsToRunInSheet = _jsToRunInSheet.asStateFlow()

    private val _isShowingSheet = MutableStateFlow(false)
    val isShowingSheet = _isShowingSheet.asStateFlow()

    // Aggiungiamo gli stati per la navigazione
    private val _navigationHistory = MutableStateFlow<List<String>>(emptyList())
    private val _bookmarkUrl = MutableStateFlow<String?>(null)
    val bookmarkUrl: StateFlow<String?> = _bookmarkUrl.asStateFlow()

    fun initialize(bookId: Int) {
        viewModelScope.launch {
            // All'avvio, carichiamo lo stato iniziale da DataStore
            val session = dataStore.data.first()

            _navigationHistory.value = session.navigationHistoryList
            _bookmarkUrl.value = session.bookmarkedParagraphUrl.ifEmpty { null }

            val bookFile = File(getApplication<Application>().filesDir, "books/$bookId/title.htm")
            val bookPath = "file://${bookFile.absolutePath}"
            _bookUrl.value = bookPath
            addUrlToHistory(bookPath) // Aggiungiamo alla cronologia

            _sheetUrl.value = when (bookId) {
                in 1..5 -> "file:///android_asset/sheets/char_sheet_301.htm"
                in 6..12 -> "file:///android_asset/sheets/char_sheet_302.htm"
                in 13..20 -> "file:///android_asset/sheets/char_sheet_303.htm"
                else -> "file:///android_asset/sheets/char_sheet_304.htm"
            }
        }
    }

    // --- Funzioni per la navigazione e il salvataggio ---

    fun onNewUrl(url: String) {
        _bookUrl.value = url
        addUrlToHistory(url)
    }

    private fun addUrlToHistory(url: String) {
        viewModelScope.launch {
            if (_navigationHistory.value.lastOrNull() != url) {
                val newHistory = _navigationHistory.value + url
                _navigationHistory.value = newHistory
                dataStore.updateData { it.toBuilder().clearNavigationHistory().addAllNavigationHistory(newHistory).build() }
            }
        }
    }

    fun onBookmarkClicked() {
        viewModelScope.launch {
            val currentUrl = _bookUrl.value
            val newBookmark = if (_bookmarkUrl.value == currentUrl) null else currentUrl
            _bookmarkUrl.value = newBookmark
            dataStore.updateData { it.toBuilder().setBookmarkedParagraphUrl(newBookmark ?: "").build() }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            if (_navigationHistory.value.size > 1) {
                val newHistory = _navigationHistory.value.dropLast(1)
                _navigationHistory.value = newHistory
                dataStore.updateData { it.toBuilder().clearNavigationHistory().addAllNavigationHistory(newHistory).build() }
                _bookUrl.value = newHistory.last()
            }
        }
    }

    fun onHomeClicked() {
        _navigationHistory.value.firstOrNull()?.let { homeUrl ->
            _bookUrl.value = homeUrl
        }
    }

    fun onGoToBookmarkClicked() {
        _bookmarkUrl.value?.let { bookmark ->
            onNewUrl(bookmark)
        }
    }

    fun toggleSheetVisibility() {
        _isShowingSheet.value = !_isShowingSheet.value
    }

    // --- Funzioni per il JavaScript Bridge ---

    fun saveData(key: String, value: String) {
        viewModelScope.launch {
            dataStore.updateData { currentSession ->
                currentSession.toBuilder().putSheetData(key, value).build()
            }
        }
    }

    suspend fun getAllSheetData(): Map<String, String> {
        return dataStore.data.first().sheetDataMap
    }

    fun runJsInSheetView(script: String) {
        _jsToRunInSheet.value = script
    }

    fun onJsExecuted() {
        _jsToRunInSheet.value = null
    }
}