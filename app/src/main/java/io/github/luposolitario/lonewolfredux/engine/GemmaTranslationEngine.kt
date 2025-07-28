package io.github.luposolitario.lonewolfredux.engine

import android.content.Context
import android.util.Log
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import io.github.luposolitario.lonewolfredux.data.NarrativeTone
import io.github.luposolitario.lonewolfredux.datastore.ModelSettingsManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Motore di traduzione che utilizza MediaPipe e un modello Gemma locale.
 * Versione definitiva con gestione corretta del ciclo di vita dell'Executor.
 */
class GemmaTranslationEngine(private val context: Context) {
    private val tag = "GemmaTranslationEngine"
    private var llmInference: LlmInference? = null
    // L'executor viene creato una sola volta e vive con il motore.
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    suspend fun setupGemma() {
        // --- MODIFICA CHIAVE 1: Chiudiamo solo l'istanza del modello, NON l'executor ---
        closeInferenceInstance()

        val settings = ModelSettingsManager.getSettingsFlow(context).first()
        val modelPath = settings.dmModelFilePath

        if (modelPath.isBlank() || !File(modelPath).exists()) {
            val errorMessage = "Modello Gemma non trovato o percorso non valido: $modelPath"
            Log.e(tag, errorMessage)
            throw IllegalStateException(errorMessage)
        }

        try {
            val inferenceOptions = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(settings.gemmaNLen.toIntOrNull() ?: 2048)
                .build()

            llmInference = LlmInference.createFromOptions(context, inferenceOptions)
            Log.d(tag, "Motore Gemma (LlmInference) caricato con successo.")

        } catch (e: Exception) {
            Log.e(tag, "Errore critico durante la creazione di LlmInference.", e)
            closeInferenceInstance() // In caso di errore, puliamo solo l'istanza.
            throw e
        }
    }

    suspend fun translateNarrative(currentText: String, historyText: String): Flow<String> = callbackFlow {
        val llmInferenceInstance = llmInference
        // Aggiungiamo un controllo per essere sicuri che l'executor sia attivo
        if (llmInferenceInstance == null || executor.isShutdown) {
            val errorMessage = "[ERRORE: Motore Gemma non inizializzato o executor terminato]"
            trySend(errorMessage); close(IllegalStateException(errorMessage)); return@callbackFlow
        }

        val settings = ModelSettingsManager.getSettingsFlow(context).first()
        val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
            .setTemperature(settings.gemmaTemperature.toFloatOrNull() ?: 0.9f)
            .setTopK(settings.gemmaTopK.toIntOrNull() ?: 50)
            .setTopP(settings.gemmaTopP.toFloatOrNull() ?: 1.0f)
            .build()

        val session = LlmInferenceSession.createFromOptions(llmInferenceInstance, sessionOptions)
        val finalPrompt = buildPrompt(currentText, historyText, NarrativeTone.fromKey(settings.narrativeTone))

        try {
            session.addQueryChunk(finalPrompt)
            val future = session.generateResponseAsync()

            Futures.addCallback(future, object : FutureCallback<String> {
                override fun onSuccess(result: String?) {
                    if (result != null) {
                        trySend(result)
                    }
                    close()
                }

                override fun onFailure(t: Throwable) {
                    Log.e(tag, "Errore durante la generazione della risposta.", t)
                    trySend("[ERRORE DI TRADUZIONE]")
                    close(t)
                }
            }, executor) // Ora l'executor è garantito essere attivo

        } catch (e: Exception) {
            Log.e(tag, "Errore imprevisto in generateResponseAsync", e)
            close(e)
        }

        awaitClose {
            session.close()
        }
    }

    private fun buildPrompt(currentText: String, historyText: String, narrativeTone: NarrativeTone): String {
        val toneInstruction = when (narrativeTone) {
            NarrativeTone.Neutro -> "Traduci il testo in italiano in modo fedele."
            else -> "Traduci il testo in italiano adottando uno stile narrativo '${narrativeTone.displayName}'."
        }

        val finalPrompt = """
        Sei un traduttore esperto di librogame specializzato in HTML.
        Il tuo compito è tradurre il testo dall'inglese all'italiano, mantenendo la struttura dei tag HTML intatta.

        ISTRUZIONI:
        - Traduci solo il contenuto testuale.
        - Quando trovi il nome : Lone Wolf lo traduci sempre con Lupo Solitario
        - Quando trovi The Story So Far lo traduci con La storia fino ad ora 
        - Mantieni ogni tag HTML (`<p>`, `<strong>`, `<cite>`, `<a>`, ecc.) esattamente com'è nell'originale.
        - La tua risposta deve essere SOLO l'HTML tradotto, senza ``` o altri commenti.
        
        $toneInstruction

        [CONTESTO STORIA PRECEDENTE]
        $historyText
        ---
        [TESTO HTML DA TRADURRE]
        $currentText
        ---
        TRADUZIONE HTML:
        """.trimIndent()
        // --- PROMPT RIEQUILIBRATO ---
        // --- FINE MODIFICA ---

        Log.d(tag, "--- PROMPT FINALE PER GEMMA ---")
        Log.d(tag, finalPrompt)
        Log.d(tag, "---------------------------------")
        return finalPrompt
    }

    /**
     * Chiude solo l'istanza di LlmInference.
     */
    private fun closeInferenceInstance() {
        llmInference?.close()
        llmInference = null
        Log.d(tag, "Istanza LlmInference rilasciata.")
    }

    /**
     * Rilascia TUTTE le risorse. Da chiamare solo in ViewModel.onCleared().
     */
    fun shutdown() {
        closeInferenceInstance()
        if (!executor.isShutdown) {
            executor.shutdown()
            Log.d(tag, "ExecutorService di GemmaTranslationEngine terminato.")
        }
    }
}