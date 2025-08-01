package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import android.util.Log
import android.webkit.JsResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import io.github.luposolitario.lonewolfredux.datastore.AppSettings
import io.github.luposolitario.lonewolfredux.datastore.AppSettingsManager
import io.github.luposolitario.lonewolfredux.datastore.GameSession
import io.github.luposolitario.lonewolfredux.datastore.SaveGameManager
import io.github.luposolitario.lonewolfredux.datastore.SaveSlotInfo
import io.github.luposolitario.lonewolfredux.engine.GemmaTranslationEngine
import io.github.luposolitario.lonewolfredux.engine.TranslationEngine
import io.github.luposolitario.lonewolfredux.service.TtsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.cancellation.CancellationException


data class ConfirmDialogState(val title: String, val message: String, val result: JsResult)


class GameViewModel(
    application: Application,
    private val gemmaEngine: GemmaTranslationEngine
) : AndroidViewModel(application) {

    private var translationJob: Job? = null
    private var activeSlotId: Int = 1
    private var currentBookId: Int = -1
    private val translationEngine = TranslationEngine()
    private val _jsToRunInBook = MutableStateFlow<String?>(null)
    val jsToRunInBook: StateFlow<String?> = _jsToRunInBook.asStateFlow()
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
    private val _targetLanguage = MutableStateFlow("it")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()
    val fontZoomLevel: StateFlow<Int> = AppSettingsManager.getFontZoomLevelFlow(getApplication())
        .stateIn(viewModelScope, SharingStarted.Eagerly, 100)
    private val _showZoomSlider = MutableStateFlow(false)
    val showZoomSlider: StateFlow<Boolean> = _showZoomSlider.asStateFlow()
    val appSettings: StateFlow<AppSettings> =
        AppSettingsManager.getTtsSettingsFlow(getApplication())
            .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings.getDefaultInstance())
    private var ttsService: TtsService? = null
    private var storyContextHtml: String = ""
    private val _isLoadingTranslation = MutableStateFlow(false)
    val isLoadingTranslation = _isLoadingTranslation.asStateFlow()
    private val _translatedContent = MutableStateFlow<Map<String, String>?>(null)
    val translatedContent: StateFlow<Map<String, String>?> = _translatedContent.asStateFlow()
    // --- NUOVO STATO PER IL DIALOGO DI CONFERMA ---
    private val _sheetConfirmDialogState = MutableStateFlow<ConfirmDialogState?>(null)
    val sheetConfirmDialogState: StateFlow<ConfirmDialogState?> = _sheetConfirmDialogState.asStateFlow()
    // Aggiungi queste due nuove proprietà alla classe
    private val _sheetDialogState = MutableStateFlow<Pair<String, String>?>(null)
    val sheetDialogState: StateFlow<Pair<String, String>?> = _sheetDialogState.asStateFlow()


    /**
     * Chiamata dal WebChromeClient quando riceve un `onJsConfirm`.
     */
    fun showSheetConfirmDialog(title: String, message: String, result: JsResult) {
        _sheetConfirmDialogState.value = ConfirmDialogState(title, message, result)
    }

    /**
     * Chiamata dalla UI quando l'utente preme "Conferma" o "Annulla".
     */
    fun onSheetConfirmDialogResult(confirmed: Boolean) {
        // Prende il risultato JS dallo stato, invia la risposta e pulisce lo stato.
        _sheetConfirmDialogState.value?.result?.let {
            if (confirmed) {
                it.confirm()
            } else {
                it.cancel()
            }
        }
        _sheetConfirmDialogState.value = null // Nasconde il dialogo
    }

    // Aggiungi queste due nuove funzioni
    fun showSheetDialog(title: String, message: String) {
        _sheetDialogState.value = Pair(title, message)
    }

    fun dismissSheetDialog() {
        _sheetDialogState.value = null
    }

    init {
        ttsService = TtsService(application) {
            Log.d("GameViewModel", "Motore TTS pronto.")
        }
        viewModelScope.launch {
            try {
                gemmaEngine.setupGemma()
            } catch (e: Exception) {
                Log.e("GameViewModel", "Setup di Gemma fallito", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsService?.shutdown()
        gemmaEngine.shutdown()
    }

    fun setPreviousStoryContext(html: String) {
        storyContextHtml = html
    }

    fun translateParagraphs(paragraphs: List<Pair<String, String>>) {
        translationJob?.cancel()
        translationJob = viewModelScope.launch {
            _isLoadingTranslation.value = true

            val results = mutableMapOf<String, String>()
            var paragraphContext = ""

            for (paragraph in paragraphs) {
                if (!isActive) break

                val paragraphId = paragraph.first
                val paragraphHtml = paragraph.second

                gemmaEngine.translateNarrative(paragraphHtml, paragraphContext)
                    .catch { exception ->
                        if (exception !is CancellationException) {
                            Log.e("GameViewModel", "Errore su paragrafo $paragraphId", exception)
                            results[paragraphId] = paragraphHtml
                        }
                    }
                    .collect { translatedHtml ->
                        paragraphContext = translatedHtml
                        results[paragraphId] = translatedHtml
                    }
            }

            if (isActive) {
                _translatedContent.value = results
            }

            _isLoadingTranslation.value = false
        }
    }

    fun consumeTranslatedContent() {
        _translatedContent.value = null
    }

    fun initialize(bookId: Int) {
        if (bookId <= 0) {
            Log.e("GameViewModel", "ID del libro non valido: $bookId.")
            return
        }
        currentBookId = bookId
        viewModelScope.launch {
            _isCurrentBookCompleted.value =
                AppSettingsManager.isBookCompleted(getApplication(), currentBookId)
            AppSettingsManager.getTargetLanguageFlow(getApplication()).collect {
                _targetLanguage.value = it
            }
        }
        loadGame(1)
    }

    fun onTranslateRequest(text: String, callbackId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetLang = _targetLanguage.value
            if (targetLang == "en" || text.isBlank()) {
                val escapedText = text.replace("'", "\\'")
                val script = "window.onTranslationResult('$escapedText', $callbackId);"
                runJsInBookView(script)
                return@launch
            }
            val translatedText = translationEngine.translate(text, targetLanguage.value)
            val escapedText = translatedText.replace("'", "\\'")
            val script = "window.onTranslationResult('$escapedText', $callbackId);"
            runJsInBookView(script)
        }
    }

    fun speakText(text: String) {
        ttsService?.speak(text, appSettings.value)
    }

    fun onZoomChange(zoomLevel: Int) {
        viewModelScope.launch {
            AppSettingsManager.setFontZoomLevel(getApplication(), zoomLevel)
        }
    }

    fun openZoomSlider() {
        _showZoomSlider.value = true
    }

    fun closeZoomSlider() {
        _showZoomSlider.value = false
    }

    fun onSheetTranslateRequest(text: String, callbackId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetLang = _targetLanguage.value
            if (targetLang == "en" || text.isBlank()) {
                val escapedText = text.replace("'", "\\'")
                val script = "window.onTranslationResult('$escapedText', $callbackId);"
                runJsInSheetView(script)
                return@launch
            }
            val translatedText = translationEngine.translate(text, targetLanguage.value)
            val escapedText = translatedText.replace("'", "\\'")
            val script = "window.onTranslationResult('$escapedText', $callbackId);"
            runJsInSheetView(script)
        }
    }

    fun runJsInBookView(script: String) {
        _jsToRunInBook.value = script
    }

    fun onBookJsExecuted() {
        _jsToRunInBook.value = null
    }

    fun toggleBookCompletion() {
        if (currentBookId > 0) {
            viewModelScope.launch {
                AppSettingsManager.toggleBookCompletion(getApplication(), currentBookId)
                _isCurrentBookCompleted.value =
                    AppSettingsManager.isBookCompleted(getApplication(), currentBookId)
            }
        }
    }

    fun loadGame(slotId: Int) {
        viewModelScope.launch {
            activeSlotId = slotId
            val session = SaveGameManager.getSession(getApplication(), currentBookId, activeSlotId)
            _navigationHistory.value = session.navigationHistoryList
            _bookmarkUrl.value = session.bookmarkedParagraphUrl.ifEmpty { null }
            val urlToLoad = if (session.navigationHistoryList.isEmpty()) {
                val bookFile =
                    File(getApplication<Application>().filesDir, "books/$currentBookId/title.htm")
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
        Log.d("GameViewModel", "Dati estratti per salvataggio: libro $currentBookId, slot $activeSlotId")
        viewModelScope.launch {
            val dataMap: Map<String, String> = Gson().fromJson(
                jsonData,
                object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
            )
            val sessionToSave = GameSession.newBuilder()
                .putAllSheetData(dataMap)
                .clearNavigationHistory()
                .addAllNavigationHistory(_navigationHistory.value)
                .setBookmarkedParagraphUrl(_bookmarkUrl.value ?: "")
                .build()
            SaveGameManager.updateSession(getApplication(), currentBookId, activeSlotId, sessionToSave)
            Log.d("GameViewModel", "Salvataggio completato")
            refreshSaveSlots()
            closeSaveLoadDialog()
        }
    }

    fun loadSheetDataIntoWebView(callbackId: String) {
        viewModelScope.launch {
            val dataMap = SaveGameManager.getSession(getApplication(), currentBookId, activeSlotId).sheetDataMap
            val jsonData = Gson().toJson(dataMap)
            val escapedJsonData = jsonData.replace("'", "\\'")
            val script = "window.nativeCallback('$callbackId', '$escapedJsonData');"
            runJsInSheetView(script)
        }
    }

    private suspend fun refreshSaveSlots() {
        _saveSlots.value = SaveGameManager.getSaveSlotsInfo(getApplication(), currentBookId)
    }

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
        if (translationJob?.isActive == true) {
            translationJob?.cancel()
            Log.d("GameViewModel", "Traduzione precedente annullata a causa di nuova navigazione.")
        }
        // --- INIZIO MODIFICA ---
        // Aggiungi l'URL alla cronologia solo se non è già l'ultimo
        if (_navigationHistory.value.lastOrNull() != url) {
            _navigationHistory.value = _navigationHistory.value + url
            autoSaveCurrentSession() // Salva la sessione dopo aver aggiornato la cronologia
        }
        // --- FINE MODIFICA ---
        _bookUrl.value = url
    }

    fun addUrlToHistory(url: String) {
        if (_navigationHistory.value.lastOrNull() != url) {
            _navigationHistory.value = _navigationHistory.value + url
        }
    }

    // --- FUNZIONE MODIFICATA ---
    fun onHomeClicked() {
        if (currentBookId > 0) {
            val homeFile = File(getApplication<Application>().filesDir, "books/$currentBookId/sect1.htm")
            val homeUrl = "file://${homeFile.absolutePath}"

            // Notifica il cambio di URL...
            _navigationHistory.value = listOf(homeUrl) // La cronologia viene resettata
            autoSaveCurrentSession() // <-- Aggiungi questa chiamata

            onNewUrl(homeUrl)
        }
    }

    fun onBackClicked() {
        if (_navigationHistory.value.size > 1) {
            val newHistory = _navigationHistory.value.dropLast(1)
            val previousUrl = newHistory.last()
            _navigationHistory.value = newHistory
            // Usa onNewUrl per mantenere la logica di cancellazione
            autoSaveCurrentSession() // <-- Aggiungi questa chiamata
            onNewUrl(previousUrl)
        }
    }

    // In: GameViewModel.kt

    private fun autoSaveCurrentSession() {
        viewModelScope.launch {
            // Recupera la sessione corrente per non perdere i dati della scheda azione
            val currentSession = SaveGameManager.getSession(getApplication(), currentBookId, activeSlotId)

            // Crea una sessione aggiornata con la cronologia e il segnalibro attuali
            val updatedSession = currentSession.toBuilder()
                .clearNavigationHistory()
                .addAllNavigationHistory(_navigationHistory.value)
                .setBookmarkedParagraphUrl(_bookmarkUrl.value ?: "")
                .build()

            // Sovrascrive la sessione su disco con i dati aggiornati
            SaveGameManager.updateSession(getApplication(), currentBookId, activeSlotId, updatedSession)
            Log.d("GameViewModel", "Sessione auto-salvata per lo slot $activeSlotId")
        }
    }


    fun onBookmarkClicked() {
        val currentUrl = _bookUrl.value
        _bookmarkUrl.value = if (_bookmarkUrl.value == currentUrl) null else currentUrl
        autoSaveCurrentSession() // <-- Aggiungi questa chiamata
    }

    fun onGoToBookmarkClicked() {
        _bookmarkUrl.value?.let { bookmarkedUrl ->
            onNewUrl(bookmarkedUrl)
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