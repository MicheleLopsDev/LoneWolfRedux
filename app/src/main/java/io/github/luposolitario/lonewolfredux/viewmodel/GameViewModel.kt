package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.luposolitario.lonewolfredux.datastore.GameSession
import io.github.luposolitario.lonewolfredux.datastore.GameSessionSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Definiamo il nostro DataStore come estensione del Context.
// Questo crea un unico file di salvataggio per l'app.
private val Context.gameSessionDataStore: DataStore<GameSession> by dataStore(
    fileName = "game_session.pb",
    serializer = GameSessionSerializer
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.gameSessionDataStore

    // Stati per la UI
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


    fun initialize(bookId: Int) {
        viewModelScope.launch {
            // All'avvio, carichiamo lo stato salvato.
            val session = dataStore.data.first()
            _navigationHistory.value = session.navigationHistoryList
            _bookmarkUrl.value = session.bookmarkedParagraphUrl.ifEmpty { null }

            // Se la cronologia è vuota, carichiamo la pagina del titolo.
            // Altrimenti, carichiamo l'ultima pagina visitata.
            val urlToLoad = if (session.navigationHistoryList.isEmpty()) {
                val bookFile = File(getApplication<Application>().filesDir, "books/$bookId/title.htm")
                "file://${bookFile.absolutePath}"
            } else {
                session.navigationHistoryList.last()
            }

            _bookUrl.value = urlToLoad
            if (session.navigationHistoryList.isEmpty()) {
                addUrlToHistory(urlToLoad)
            }

            _sheetUrl.value = when (bookId) {
                in 1..5 -> "file:///android_asset/sheets/char_sheet_301.htm"
                in 6..12 -> "file:///android_asset/sheets/char_sheet_302.htm"
                in 13..20 -> "file:///android_asset/sheets/char_sheet_303.htm"
                else -> "file:///android_asset/sheets/char_sheet_304.htm"
            }
        }
    }

    // --- INIZIO BLOCCO DI SALVATAGGIO/CARICAMENTO NATIVO ---

    // Chiamato dal pulsante "Salva" nativo
    fun onSaveSheetClicked() {
        // Script per estrarre i dati dal form e chiamare il nostro ponte
        val script = """
            (function() {
                var data = {};
                var form = document.actionChart;
                for (var i = 0; i < form.elements.length; i++) {
                    var field = form.elements[i];
                    if (field.name) {
                        data[field.name] = (field.type === 'checkbox') ? field.checked : field.value;
                    }
                }
                window.Android.onSheetDataExtracted(JSON.stringify(data));
            })();
        """.trimIndent()
        runJsInSheetView(script)
    }

    // Chiamato dal ponte JS con i dati estratti
    fun saveSheetData(jsonData: String) {
        Log.d("GameViewModel", "Dati estratti ricevuti: $jsonData")
        viewModelScope.launch {
            val type = object : TypeToken<Map<String, String>>() {}.type
            val dataMap: Map<String, String> = Gson().fromJson(jsonData, type)
            dataStore.updateData { currentSession ->
                currentSession.toBuilder()
                    .clearSheetData()
                    .putAllSheetData(dataMap)
                    .build()
            }
        }
    }

    // Funzione per preparare lo script di caricamento
    fun prepareLoadScript() {
        viewModelScope.launch {
            val dataMap = getAllSheetData()
            if (dataMap.isNotEmpty()) {
                val jsonData = Gson().toJson(dataMap)
                val script = """
                    (function() {
                        var data = JSON.parse('$jsonData');
                        var form = document.actionChart;
                        for (var i = 0; i < form.elements.length; i++) {
                            var field = form.elements[i];
                            if (field.name && data[field.name] !== undefined) {
                                if (field.type === 'checkbox') {
                                    field.checked = (data[field.name] === 'true');
                                } else {
                                    field.value = data[field.name];
                                }
                            }
                        }
                        if (typeof findPercentage === 'function') findPercentage();
                    })();
                """.trimIndent()
                runJsInSheetView(script)
            }
        }
    }

    // --- FINE BLOCCO ---

    fun onNewUrl(url: String) {
        _bookUrl.value = url
        addUrlToHistory(url)
    }

    private fun addUrlToHistory(url: String) {
        viewModelScope.launch {
            if (_navigationHistory.value.lastOrNull() != url) {
                val newHistory = _navigationHistory.value + url
                _navigationHistory.value = newHistory
                // Salva la nuova cronologia su disco
                dataStore.updateData { it.toBuilder().clearNavigationHistory().addAllNavigationHistory(newHistory).build() }
            }
        }
    }

    fun onHomeClicked() {
        _navigationHistory.value.firstOrNull()?.let { homeUrl ->
            _bookUrl.value = homeUrl
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            if (_navigationHistory.value.size > 1) {
                val newHistory = _navigationHistory.value.dropLast(1)
                _navigationHistory.value = newHistory
                // Salva la cronologia accorciata
                dataStore.updateData { it.toBuilder().clearNavigationHistory().addAllNavigationHistory(newHistory).build() }
                _bookUrl.value = newHistory.last()
            }
        }
    }

    fun onBookmarkClicked() {
        viewModelScope.launch {
            val currentUrl = _bookUrl.value
            val newBookmark = if (_bookmarkUrl.value == currentUrl) null else currentUrl
            _bookmarkUrl.value = newBookmark
            // Salva il segnalibro
            dataStore.updateData { it.toBuilder().setBookmarkedParagraphUrl(newBookmark ?: "").build() }
        }
    }

    fun onGoToBookmarkClicked() {
        _bookmarkUrl.value?.let { onNewUrl(it) }
    }

    fun toggleSheetVisibility() {
        _isShowingSheet.value = !_isShowingSheet.value
    }

    // Funzioni per il JavaScript Bridge
    fun saveData(key: String, value: String) {
        // --- AGGIUNGI QUESTO LOG ---
        Log.d("GameViewModel", "Salvataggio da JS ricevuto -> Chiave: $key, Valore: $value")
        // --- FINE AGGIUNTA ---
        viewModelScope.launch {
            dataStore.updateData { currentSession ->
                currentSession.toBuilder().putSheetData(key, value).build()
            }
        }
    }

    suspend fun getAllSheetData(): Map<String, String> {
        // Legge e restituisce tutti i dati della scheda
        return dataStore.data.first().sheetDataMap
    }

    fun runJsInSheetView(script: String) {
        _jsToRunInSheet.value = script
    }

    fun onJsExecuted() {
        _jsToRunInSheet.value = null
    }
}