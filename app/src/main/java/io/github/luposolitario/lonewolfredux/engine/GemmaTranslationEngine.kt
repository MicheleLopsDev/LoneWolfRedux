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
 * Versione definitiva basata sul pattern ListenableFuture per la massima stabilità.
 */
class GemmaTranslationEngine(private val context: Context) {
    private val tag = "GemmaTranslationEngine"
    private var llmInference: LlmInference? = null
    // È necessario un Executor per eseguire le callback del Future
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    suspend fun setupGemma() {
        release()
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
            release()
            throw e
        }
    }

    suspend fun translateNarrative(currentText: String, historyText: String): Flow<String> = callbackFlow {
        val llmInferenceInstance = llmInference
        if (llmInferenceInstance == null) {
            val errorMessage = "[ERRORE: Motore Gemma non inizializzato]"
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
            // Aggiungiamo il prompt alla sessione
            session.addQueryChunk(finalPrompt)

            // Chiamiamo il metodo che restituisce un Future, senza listener
            val future = session.generateResponseAsync()

            // Aggiungiamo una callback al Future per gestire il risultato
            Futures.addCallback(future, object : FutureCallback<String> {
                override fun onSuccess(result: String?) {
                    if (result != null) {
                        trySend(result) // Inviamo il risultato completo
                    }
                    close() // Operazione completata, chiudiamo il Flow
                }

                override fun onFailure(t: Throwable) {
                    Log.e(tag, "Errore durante la generazione della risposta.", t)
                    trySend("[ERRORE DI TRADUZIONE]")
                    close(t) // Chiudiamo il Flow con un errore
                }
            }, executor) // Specifichiamo l'executor

        } catch (e: Exception) {
            Log.e(tag, "Errore imprevisto in generateResponseAsync", e)
            close(e)
        }

        awaitClose {
            Log.d(tag, "Flow chiuso. Rilascio sessione.")
            session.close()
        }
    }

    private fun buildPrompt(currentText: String, historyText: String, narrativeTone: NarrativeTone): String {
        val toneInstruction = when (narrativeTone) {
            NarrativeTone.Neutro -> "Traduci il seguente testo in italiano cercando di mantenere la traduzione più fedele possibile all'opera, evitando di cambiare o aggiungere cose."
            else -> "Traduci il seguente testo in italiano adottando uno stile narrativo '${narrativeTone.displayName}'."
        }
        return """
        Sei un traduttore esperto di librogame della serie Lupo Solitario. Mantieni la massima coerenza con il contesto precedente.
        $toneInstruction

        [CONTESTO DELLA STORIA PRECEDENTE]
        $historyText
        ---
        [TESTO ATTUALE DA TRADURRE]
        $currentText
        ---
        TRADUZIONE:
        """.trimIndent()
    }

    fun release() {
        llmInference?.close()
        llmInference = null
        if (!executor.isShutdown) {
            executor.shutdown()
        }
        Log.d(tag, "Risorse LlmInference rilasciate.")
    }
}