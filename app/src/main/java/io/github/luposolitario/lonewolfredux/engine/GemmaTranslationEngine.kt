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
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import io.github.luposolitario.lonewolfredux.datastore.TranslationCacheManager // Importa il nuovo manager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GemmaTranslationEngine(private val context: Context) {
    private val tag = "GemmaTranslationEngine"
    private var llmInference: LlmInference? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    suspend fun setupGemma() {
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
                //.setPreferredBackend(LlmInference.Backend.GPU)
                .setMaxTokens(settings.gemmaNLen.toIntOrNull() ?: 2048)
                .build()
            llmInference = LlmInference.createFromOptions(context, inferenceOptions)
            Log.d(tag, "Motore Gemma (LlmInference) caricato con successo.")
        } catch (e: Exception) {
            Log.e(tag, "Errore critico durante la creazione di LlmInference.", e)
            closeInferenceInstance()
            throw e
        }
    }

    suspend fun translateNarrative(currentText: String, historyText: String): Flow<String> = callbackFlow {
        // --- MODIFICA CHIAVE 2: USIAMO I METODI DELLA LRUCACHE ---
        // --- MODIFICA CHIAVE: CONTROLLO DELLA CACHE PERSISTENTE ---
        val cachedTranslation = TranslationCacheManager.getTranslation(context, currentText)
        if (cachedTranslation != null) {
            Log.d(tag, "Cache HIT (dal disco). Restituisco la traduzione salvata.")
            trySend(cachedTranslation)
            close()
            return@callbackFlow
        }

        Log.d(tag, "Cache MISS. Avvio traduzione con Gemma.")

        val llmInferenceInstance = llmInference
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
        Log.d(tag, finalPrompt)
        try {
            session.addQueryChunk(finalPrompt)
            val future = session.generateResponseAsync()

            Futures.addCallback(future, object : FutureCallback<String> {
                override fun onSuccess(result: String?) {
                    if (result != null) {
                        // --- MODIFICA CHIAVE 3: SALVIAMO NELLA LRUCACHE ---
                        engineScope.launch {
                            TranslationCacheManager.saveTranslation(context, currentText, result)
                            Log.d(tag, "Traduzione salvata nella cache persistente.")
                        }
                        Log.d(tag, result)
                        trySend(result)
                    }
                    close()
                }
                override fun onFailure(t: Throwable) {
                    // Se l'errore è un annullamento, è un comportamento atteso, non un vero errore.
                    if (t is CancellationException) {
                        Log.d(tag, "Traduzione annullata con successo.")
                    } else {
                        Log.e(tag, "Errore durante la generazione della risposta.", t)
                        trySend("[ERRORE DI TRADUZIONE]")
                    }
                    close(t)
                }
            }, executor)

            // Quando il Flow viene annullato, cancelliamo il Future
            awaitClose {
                future.cancel(true)
            }

        } catch (e: Exception) {
            if (e !is CancellationException) {
                Log.e(tag, "Errore imprevisto in generateResponseAsync", e)
            }
            close(e)
        }
    }


    private fun buildPrompt(currentText: String, historyText: String, narrativeTone: NarrativeTone): String {
        val toneInstruction = when (narrativeTone) {
            NarrativeTone.Neutro -> "Traduci il testo in italiano in modo fedele."
            else -> "Traduci il testo in italiano adottando uno stile narrativo '${narrativeTone.displayName}'."
        }

        // --- PROMPT AGGIORNATO CON LE TUE REGOLE ---
        val finalPrompt = """
        Sei un traduttore esperto di librogame specializzato in HTML.
        Il tuo compito è tradurre il testo dall'inglese all'italiano, mantenendo la struttura dei tag HTML intatta.

        ISTRUZIONI:
        - Traduci solo il contenuto testuale.
        - Quando trovi il nome "Lone Wolf", traducilo sempre con "Lupo Solitario".
        - NON tradurre i seguenti nomi Camouflage, Hunting, 'Sixth Sense', Tracking, Healing, Weaponskill, Mindshield, Mindblast, 'Animal Kinship', 'Mind Over Matter'.
        - quando incontri la frase turn xxx traducilo con 'vai al paragrafo ' xxx
        - Mantieni ogni tag HTML (`<p>`, `<strong>`, `<cite>`, `<a>`, ecc.) esattamente com'è nell'originale.
        - La tua risposta deve essere SOLO l'HTML tradotto, senza ``` o altri commenti.
        
        $toneInstruction

        ---
        [TESTO HTML DA TRADURRE]
        $currentText
       
        """.trimIndent()

        return finalPrompt
    }

    private fun closeInferenceInstance() {
        llmInference?.close()
        llmInference = null
    }

    fun shutdown() {
        closeInferenceInstance()
        if (!executor.isShutdown) {
            executor.shutdown()
            Log.d(tag, "ExecutorService di GemmaTranslationEngine terminato.")
        }
    }
}