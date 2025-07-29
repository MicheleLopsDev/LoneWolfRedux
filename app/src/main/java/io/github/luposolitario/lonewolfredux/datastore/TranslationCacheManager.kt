package io.github.luposolitario.lonewolfredux.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.translationCacheDataStore: DataStore<TranslationCache> by dataStore(
    fileName = "translation_cache.pb",
    serializer = TranslationCacheSerializer
)

object TranslationCacheManager {

    /**
     * Cerca una traduzione nella cache.
     * @param originalText Il testo originale da cercare.
     * @return Il testo tradotto se presente, altrimenti null.
     */
    suspend fun getTranslation(context: Context, originalText: String): String? {
        val key = originalText.hashCode().toLong()
        val cache = context.translationCacheDataStore.data.first()
        return cache.translationsMap[key]
    }

    /**
     * Salva una nuova traduzione nella cache.
     * @param originalText Il testo originale (usato per generare la chiave).
     * @param translatedText Il testo tradotto da salvare.
     */
    suspend fun saveTranslation(context: Context, originalText: String, translatedText: String) {
        val key = originalText.hashCode().toLong()
        context.translationCacheDataStore.updateData { currentCache ->
            currentCache.toBuilder()
                .putTranslations(key, translatedText)
                .build()
        }
    }

    /**
     * Svuota completamente la cache.
     */
    suspend fun clearCache(context: Context) {
        context.translationCacheDataStore.updateData {
            it.toBuilder().clearTranslations().build()
        }
    }
}