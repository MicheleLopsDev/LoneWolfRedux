package io.github.luposolitario.lonewolfredux.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.first

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
}