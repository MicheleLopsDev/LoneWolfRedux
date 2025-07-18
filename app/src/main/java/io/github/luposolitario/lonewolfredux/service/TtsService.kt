// In: main/java/io/github/luposolitario/lonewolfredux/service/TtsService.kt

package io.github.luposolitario.lonewolfredux.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import io.github.luposolitario.lonewolfredux.datastore.AppSettings
import java.util.Locale

class TtsService(
    context: Context,
    // --- MODIFICA CHIAVE: ORA IL CALLBACK ACCETTA UN BOOLEANO ---
    private val onInit: (success: Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.language = Locale.ITALIAN
            Log.d("TtsService", "TTS Engine Initialized SUCCESSFULLY.")
            onInit(true) // Comunica il successo
        } else {
            isReady = false
            Log.e("TtsService", "TTS Initialization FAILED with status code: $status")
            onInit(false) // Comunica il fallimento
        }
    }

    fun speak(text: String, settings: AppSettings) {
        if (!isReady || tts == null) return

        tts?.setSpeechRate(if (settings.ttsSpeechRate == 0f) 1.0f else settings.ttsSpeechRate)
        tts?.setPitch(if (settings.ttsPitch == 0f) 1.0f else settings.ttsPitch)

        val selectedVoiceName = settings.ttsNarratorVoice
        val voice = if (selectedVoiceName.isNotEmpty()) {
            tts?.voices?.find { it.name == selectedVoiceName }
        } else {
            null // Usa il default di sistema
        }
        tts?.voice = voice

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }


    fun getAvailableVoices(): List<Voice> {
        if (!isReady || tts == null) return emptyList()

        // --- RIPRISTINA IL FILTRO PER LA LINGUA ITALIANA ---
        // Vecchio codice: return tts?.voices?.toList() ?: emptyList()
        return tts?.voices?.filter { it.locale.language == Locale.ITALIAN.language } ?: emptyList()
    }

    fun shutdown() {
        tts?.stop(); tts?.shutdown(); tts = null; isReady = false
    }
}