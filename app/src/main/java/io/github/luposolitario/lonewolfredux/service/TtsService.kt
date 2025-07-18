// In: main/java/io/github/luposolitario/lonewolfredux/service/TtsService.kt

package io.github.luposolitario.lonewolfredux.service

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import io.github.luposolitario.lonewolfredux.datastore.AppSettings
import java.util.Locale
import java.util.UUID

class TtsService(
    private val context: Context,
    private val onInit: (success: Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isReady = false
    private var pendingText: String? = null
    private var pendingSettings: AppSettings? = null

    // --- NUOVO: Limite massimo di caratteri per chiamata a speak() ---
    private val MAX_TEXT_LENGTH = 3900

    init {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("TtsService", "Riproduzione INIZIATA per: $utteranceId")
            }
            override fun onDone(utteranceId: String?) {
                Log.d("TtsService", "Riproduzione TERMINATA per: $utteranceId")
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e("TtsService", "ERRORE durante la riproduzione per: $utteranceId")
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.language = Locale.ITALIAN
            Log.d("TtsService", "SUCCESS: Motore TTS inizializzato.")
            onInit(true)

            if (pendingText != null && pendingSettings != null) {
                speak(pendingText!!, pendingSettings!!)
                pendingText = null
                pendingSettings = null
            }
        } else {
            isReady = false
            Log.e("TtsService", "FAILURE: Inizializzazione TTS fallita. Codice: $status")
            onInit(false)
        }
    }

    fun speak(text: String, settings: AppSettings) {
        if (!isReady || tts == null) {
            Log.w("TtsService", "Motore non pronto. Metto la richiesta in attesa.")
            pendingText = text
            pendingSettings = settings
            return
        }

        // ... (codice per controllo volume e impostazioni invariato) ...
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            Log.e("TtsService", "ATTENZIONE: Il volume multimediale è a zero.")
        }

        tts?.setSpeechRate(if (settings.ttsSpeechRate == 0f) 1.0f else settings.ttsSpeechRate)
        tts?.setPitch(if (settings.ttsPitch == 0f) 1.0f else settings.ttsPitch)
        val selectedVoiceName = settings.ttsNarratorVoice
        val voice = if (selectedVoiceName.isNotEmpty()) tts?.voices?.find { it.name == selectedVoiceName } else null
        tts?.voice = voice


        if (text.length > MAX_TEXT_LENGTH) {
            Log.d("TtsService", "Testo troppo lungo (${text.length} caratteri). Lo divido in blocchi.")

            // --- MODIFICA CHIAVE: Aggiunto il punto e virgola (;) alla regex ---
            val chunks = text.split("(?<=[.!?`**;**`])\\s*".toRegex())
                .filter { it.isNotBlank() }

            tts?.speak(chunks.first(), TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
            chunks.drop(1).forEach { chunk ->
                tts?.speak(chunk, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
            }
        } else {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        }
    }

    fun getAvailableVoices(): List<Voice> {
        if (!isReady || tts == null) return emptyList()
        return tts?.voices?.filter { it.locale.language == Locale.ITALIAN.language } ?: emptyList()
    }

    fun shutdown() {
        tts?.stop(); tts?.shutdown(); tts = null; isReady = false
    }
}