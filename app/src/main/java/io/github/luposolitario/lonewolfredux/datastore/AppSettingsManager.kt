package io.github.luposolitario.lonewolfredux.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Definiamo il DataStore per le impostazioni dell'app
private val Context.appSettingsDataStore: DataStore<AppSettings> by dataStore(
    "app_settings.pb",
    AppSettingsSerializer
)

object AppSettingsManager {

    suspend fun getCompletedBookIds(context: Context): List<Int> {
        return appSettingsDataStore(context).data.first().completedBookIdsList
    }

    suspend fun toggleBookCompletion(context: Context, bookId: Int) {
        appSettingsDataStore(context).updateData { currentSettings ->
            val completedIds = currentSettings.completedBookIdsList.toMutableSet()
            if (completedIds.contains(bookId)) {
                completedIds.remove(bookId)
            } else {
                completedIds.add(bookId)
            }
            currentSettings.toBuilder().clearCompletedBookIds().addAllCompletedBookIds(completedIds).build()
        }
    }

    suspend fun isBookCompleted(context: Context, bookId: Int): Boolean {
        return getCompletedBookIds(context).contains(bookId)
    }

    // Funzione helper per accedere al DataStore in modo più pulito
    private fun appSettingsDataStore(context: Context): DataStore<AppSettings> {
        return context.appSettingsDataStore
    }

    /**
     * Resetta tutte le impostazioni dell'app al loro valore di default.
     * In questo caso, svuota la lista dei libri completati.
     */
    suspend fun clearAllSettings(context: Context) {
        context.appSettingsDataStore.updateData {
            // Ritorna un'istanza di default, che è vuota
            AppSettings.getDefaultInstance()
        }
    }

    suspend fun setTargetLanguage(context: Context, languageCode: String) {
        context.appSettingsDataStore.updateData { settings ->
            settings.toBuilder().setTargetLanguage(languageCode).build()
        }
    }

    // Restituisce il codice della lingua come Flow per osservarne i cambiamenti
    fun getTargetLanguageFlow(context: Context): Flow<String> {
        return context.appSettingsDataStore.data.map { settings ->
            // Se non è impostato, l'italiano è il default
            settings.targetLanguage.ifEmpty { "it" }
        }
    }

    // In AppSettingsManager.kt

    suspend fun setFontZoomLevel(context: Context, zoomLevel: Int) {
        context.appSettingsDataStore.updateData { settings ->
            settings.toBuilder().setFontZoomLevel(zoomLevel).build()
        }
    }

    fun getFontZoomLevelFlow(context: Context): Flow<Int> {
        return context.appSettingsDataStore.data.map { settings ->
            // Se il valore è 0 (default per int32 in proto), lo impostiamo a 100.
            if (settings.fontZoomLevel == 0) 100 else settings.fontZoomLevel
        }
    }

    fun getTtsSettingsFlow(context: Context): Flow<AppSettings> {
        // Espone l'intero oggetto AppSettings per osservare tutte le modifiche
        return context.appSettingsDataStore.data
    }

// --- FINE BLOCCO NUOVO ---

    // In AppSettingsManager.kt

    suspend fun updateTtsSettings(
        context: Context,
        rate: Float? = null,
        pitch: Float? = null,
        narratorVoice: String? = null // <-- Modificato
    ) {
        context.appSettingsDataStore.updateData { settings ->
            val builder = settings.toBuilder()
            rate?.let { builder.setTtsSpeechRate(it) }
            pitch?.let { builder.setTtsPitch(it) }
            narratorVoice?.let { builder.setTtsNarratorVoice(it) } // <-- Modificato
            builder.build()
        }
    }
}