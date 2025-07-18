// In: main/java/io/github/luposolitario/lonewolfredux/engine/TranslationEngine.kt

package io.github.luposolitario.lonewolfredux.engine

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Gestisce tutte le operazioni di traduzione usando ML Kit.
 * Supporta il download dinamico dei modelli linguistici richiesti.
 */
class TranslationEngine {
    private val tag = "TranslationEngine"

    // Cache per evitare di ricreare continuamente gli oggetti Translator
    private val translators = ConcurrentHashMap<String, Translator>()

    /**
     * Recupera (o crea e mette in cache) un traduttore per una specifica coppia di lingue.
     * Si occupa anche di scaricare il modello linguistico necessario se non è presente.
     *
     * @param sourceLang Codice della lingua di partenza (es. TranslateLanguage.ENGLISH).
     * @param targetLang Codice della lingua di destinazione (es. TranslateLanguage.FRENCH).
     * @return Un'istanza di Translator pronta all'uso.
     */
    private suspend fun getTranslator(sourceLang: String, targetLang: String): Translator {
        val key = "${sourceLang}_$targetLang"

        // Se abbiamo già un traduttore in cache, usiamolo
        if (translators.containsKey(key)) {
            return translators[key]!!
        }

        // Altrimenti, creiamone uno nuovo
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        val translator = Translation.getClient(options)

        // Mettiamo in pausa la coroutine e attendiamo che il modello sia scaricato
        suspendCancellableCoroutine<Unit> { continuation ->
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    Log.d(tag, "Modello di traduzione $sourceLang -> $targetLang scaricato con successo.")
                    if (continuation.isActive) continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    Log.e(tag, "Download del modello $sourceLang -> $targetLang fallito.", exception)
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
        }

        // Aggiungiamo il nuovo traduttore pronto alla cache e lo restituiamo
        translators[key] = translator
        return translator
    }

    /**
     * Traduce un testo dall'inglese alla lingua di destinazione specificata.
     *
     * @param text Il testo da tradurre.
     * @param targetLangCode Codice della lingua target (es. "it", "fr", "de").
     * @return Il testo tradotto, o il testo originale in caso di errore.
     */
    suspend fun translate(text: String, targetLangCode: String): String {
        if (text.isBlank()) return ""

        return try {
            // Otteniamo il traduttore (lo scaricherà se necessario)
            val translator = getTranslator(TranslateLanguage.ENGLISH, targetLangCode)

            // Eseguiamo la traduzione
            suspendCancellableCoroutine { continuation ->
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        if (continuation.isActive) continuation.resume(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(tag, "Traduzione fallita per '$text'", exception)
                        if (continuation.isActive) continuation.resume(text) // Fallback al testo originale
                    }
            }
        } catch (e: Exception) {
            Log.e(tag, "Impossibile ottenere il traduttore per $targetLangCode. Fallback al testo originale.", e)
            text
        }
    }

    /**
     * Rilascia le risorse di tutti i traduttori in cache.
     * Da chiamare quando l'engine non serve più (es. in onCleared del ViewModel).
     */
    fun close() {
        translators.values.forEach { it.close() }
        translators.clear()
        Log.d(tag, "Tutti i traduttori ML Kit sono stati chiusi.")
    }
}