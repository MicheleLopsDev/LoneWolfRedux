package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
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

    // ID dello slot di salvataggio attualmente in uso (caricato o salvato). Default a 1.
    private var activeSlotId: Int = 1
    // ID del libro attualmente in gioco. Viene impostato da initialize().
    private var currentBookId: Int = -1

    // --- STATEFLOW PER LA UI ---

    // URL del libro visualizzato nella WebView principale
    private val _bookUrl = MutableStateFlow("about:blank")
    val bookUrl: StateFlow<String> = _bookUrl.asStateFlow()

    // URL della scheda azione visualizzata nella seconda WebView
    private val _sheetUrl = MutableStateFlow("about:blank")
    val sheetUrl: StateFlow<String> = _sheetUrl.asStateFlow()

    // Script JS da eseguire nella SheetWebView (per caricare i dati, ecc.)
    private val _jsToRunInSheet = MutableStateFlow<String?>(null)
    val jsToRunInSheet: StateFlow<String?> = _jsToRunInSheet.asStateFlow()

    // Controlla se il cassetto con la Scheda Azione è aperto
    private val _isShowingSheet = MutableStateFlow(false)
    val isShowingSheet: StateFlow<Boolean> = _isShowingSheet.asStateFlow()

    // Cronologia di navigazione (lista di URL dei paragrafi visitati)
    private val _navigationHistory = MutableStateFlow<List<String>>(emptyList())
    // URL del paragrafo salvato come segnalibro
    private val _bookmarkUrl = MutableStateFlow<String?>(null)
    val bookmarkUrl: StateFlow<String?> = _bookmarkUrl.asStateFlow()

    // Lista di informazioni sugli slot di salvataggio per il dialog
    private val _saveSlots = MutableStateFlow<List<SaveSlotInfo>>(emptyList())
    val saveSlots: StateFlow<List<SaveSlotInfo>> = _saveSlots.asStateFlow()

    // Controlla la visibilità del dialog per salvare/caricare
    private val _showSaveLoadDialog = MutableStateFlow(false)
    val showSaveLoadDialog: StateFlow<Boolean> = _showSaveLoadDialog.asStateFlow()


    // --- FUNZIONI DI INIZIALIZZAZIONE E GESTIONE DEL GIOCO ---

    /**
     * Punto di ingresso principale. Chiamato da GameActivity quando si avvia un libro.
     * Carica di default il gioco dallo slot 1.
     */
    fun initialize(bookId: Int) {
        currentBookId = bookId
        // All'avvio di un nuovo libro, carichiamo sempre dallo slot 1.
        // L'utente potrà poi caricare un altro slot manualmente.
        loadGame(1)
    }

    /**
     * Carica lo stato del gioco da uno slot specifico.
     */
    fun loadGame(slotId: Int) {
        viewModelScope.launch {
            activeSlotId = slotId // Imposta il nuovo slot come attivo
            val session = SaveGameManager.getSession(getApplication(), activeSlotId)

            // Carica la cronologia e il segnalibro dallo stato
            _navigationHistory.value = session.navigationHistoryList
            _bookmarkUrl.value = session.bookmarkedParagraphUrl.ifEmpty { null }

            // Decide quale URL caricare: l'ultimo visitato o la pagina iniziale
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

            // Imposta l'URL della Scheda Azione in base all'ID del libro
            _sheetUrl.value = when (currentBookId) {
                in 1..5 -> "file:///android_asset/sheets/char_sheet_301.htm"
                in 6..12 -> "file:///android_asset/sheets/char_sheet_302.htm"
                in 13..20 -> "file:///android_asset/sheets/char_sheet_303.htm"
                else -> "file:///android_asset/sheets/char_sheet_304.htm"
            }

            closeSaveLoadDialog() // Chiude il dialog se era aperto

            // --- INIZIO MODIFICA ---
            // Dopo aver caricato i dati nel ViewModel, diciamo alla WebView
            // di eseguire la sua funzione di caricamento per aggiornare la UI.
            Log.d("GameViewModel", "Avvio del caricamento JS per lo slot $activeSlotId")
            runJsInSheetView("loadAllData();")
            // --- FINE MODIFICA ---
        }
    }

    /**
     * Avvia il processo di salvataggio per uno slot specifico.
     * Non salva direttamente, ma chiede alla WebView di estrarre i dati.
     */
    fun saveGame(slotId: Int) {
        viewModelScope.launch {
            activeSlotId = slotId // Imposta lo slot su cui si sta salvando come attivo
            // Script JS per estrarre tutti i dati del form dalla Scheda Azione
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
                    // Una volta estratti, i dati vengono passati alla funzione del bridge
                    window.Android.onSheetDataExtracted(JSON.stringify(data));
                })();
            """.trimIndent()
            runJsInSheetView(script)
        }
    }

    /**
     * Funzione chiamata dal bridge JS dopo che i dati della scheda sono stati estratti.
     * Salva l'intera sessione di gioco (dati scheda + cronologia + segnalibro) su DataStore.
     */
    fun onSheetDataExtracted(jsonData: String) {
        Log.d("GameViewModel", "Dati estratti per il salvataggio nello slot $activeSlotId")
        viewModelScope.launch {
            val dataMap: Map<String, String> = Gson().fromJson(jsonData, object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type)

            // Crea la nuova sessione da salvare, includendo TUTTI i dati attuali
            val sessionToSave = GameSession.newBuilder()
                .putAllSheetData(dataMap)
                .clearNavigationHistory()
                .addAllNavigationHistory(_navigationHistory.value)
                .setBookmarkedParagraphUrl(_bookmarkUrl.value ?: "")
                .build()

            // Salva la sessione usando il SaveGameManager
            SaveGameManager.updateSession(getApplication(), activeSlotId, sessionToSave)
            Log.d("GameViewModel", "Salvataggio completato per lo slot $activeSlotId")

            refreshSaveSlots() // Aggiorna le info del dialog per mostrare il nuovo timestamp
            closeSaveLoadDialog() // Chiude il dialog dopo il salvataggio
        }
    }


    // --- FUNZIONI DI GESTIONE DEL DIALOG ---

    fun openSaveLoadDialog() {
        viewModelScope.launch {
            refreshSaveSlots()
            _showSaveLoadDialog.value = true
        }
    }

    fun closeSaveLoadDialog() {
        _showSaveLoadDialog.value = false
    }

    private suspend fun refreshSaveSlots() {
        _saveSlots.value = SaveGameManager.getSaveSlotsInfo(getApplication())
    }

    // --- NUOVA FUNZIONE PER CARICARE I DATI NELLA SHEETWEBVIEW ---
    /**
     * Prepara lo script per popolare la SheetWebView con i dati dello slot attivo.
     */
    fun prepareSheetForDisplay() {
        viewModelScope.launch {
            val dataMap = SaveGameManager.getSession(getApplication(), activeSlotId).sheetDataMap
            if (dataMap.isNotEmpty()) {
                val jsonData = Gson().toJson(dataMap)
                val script = """
                    (function() {
                        var data = JSON.parse('$jsonData');
                        var form = document.actionChart;
                        if (!form) return; // Esce se il form non è ancora pronto
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
                        // Chiama findPercentage se esiste, per aggiornare la barra EP
                        if (typeof findPercentage === 'function') {
                            findPercentage();
                        }
                    })();
                """.trimIndent()
                runJsInSheetView(script)
            }
        }
    }

    /**
     * Recupera i dati della scheda per lo slot attivo e li restituisce al JS
     * tramite una funzione di callback.
     * @param callbackId L'ID da usare per invocare la callback in JS.
     */
    fun loadSheetDataIntoWebView(callbackId: String) {
        viewModelScope.launch {
            val dataMap = SaveGameManager.getSession(getApplication(), activeSlotId).sheetDataMap
            val jsonData = Gson().toJson(dataMap)
            // Facciamo l'escape degli apici singoli per sicurezza
            val escapedJsonData = jsonData.replace("'", "\\'")
            val script = "window.nativeCallback('$callbackId', '$escapedJsonData');"
            runJsInSheetView(script)
        }
    }

    /**
     * Chiamato quando la WebView principale naviga a un nuovo URL.
     */
    fun onNewUrl(url: String) {
        addUrlToHistory(url)
    }

    /**
     * Aggiunge un URL alla cronologia in memoria. Non salva su disco.
     */
    private fun addUrlToHistory(url: String) {
        if (_navigationHistory.value.lastOrNull() != url) {
            _navigationHistory.value = _navigationHistory.value + url
        }
    }

    fun onHomeClicked() {
        if (_navigationHistory.value.isNotEmpty()) {
            val homeUrl = _navigationHistory.value.first()
            _bookUrl.value = homeUrl
            // Resettiamo la cronologia per contenere solo la home
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
        // Se il segnalibro è già su questa pagina, lo rimuove, altrimenti lo imposta
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


    // --- FUNZIONI DI COMUNICAZIONE CON LE WEBVIEW ---

    /**
     * Esegue uno script JS nella SheetWebView.
     */
    fun runJsInSheetView(script: String) {
        _jsToRunInSheet.value = script
    }

    /**
     * Resetta il comando JS dopo che è stato eseguito per evitare riesecuzioni.
     */
    fun onJsExecuted() {
        _jsToRunInSheet.value = null
    }

    /**
     * Chiamato dal bridge JS per caricare i dati all'avvio della Scheda Azione.
     */
    suspend fun getAllSheetData(): Map<String, String> {
        return SaveGameManager.getSession(getApplication(), activeSlotId).sheetDataMap
    }
}