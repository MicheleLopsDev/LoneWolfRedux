package io.github.luposolitario.lonewolfredux.engine

import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TranslationEngine {
    // Per ora, un semplice wrapper per ML Kit
    suspend fun translate(text: String): String = suspendCoroutine { continuation ->
        if (text.isBlank()) {
            continuation.resume(text)
            return@suspendCoroutine
        }
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.ITALIAN)
            .build()
        val translator = Translation.getClient(options)

        // Assicura che il modello sia scaricato
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translatedText -> continuation.resume(translatedText) }
                    .addOnFailureListener { e ->
                        Log.e("TranslationEngine", "Translation failed", e)
                        continuation.resume(text) // Ritorna il testo originale in caso di errore
                    }
            }
            .addOnFailureListener { e ->
                Log.e("TranslationEngine", "Model download failed", e)
                continuation.resume(text)
            }
    }
}