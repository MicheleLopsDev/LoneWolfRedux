package io.github.luposolitario.lonewolfredux.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// --- DEFINIZIONE DEL DATASTORE ---
private const val DATA_STORE_FILE_NAME = "model_settings.pb"

private val Context.settingsDataStore: DataStore<ModelSettings> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = ModelSettingsSerializer
)

/**
 * Gestisce tutte le operazioni di lettura e scrittura per ModelSettings su DataStore.
 *
 * @param context Il contesto dell'applicazione, necessario per accedere al DataStore.
 */
object ModelSettingsManager{


    /**
     * NUOVO: Espone un Flow con l'oggetto ModelSettings completo.
     * La UI si aggiornerà automaticamente ad ogni cambiamento dei dati.
     */
    fun getSettingsFlow(context: Context): Flow<ModelSettings> {
        return context.settingsDataStore.data
    }


    suspend fun updateHuggingFaceToken(token: String,context: Context) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setHuggingFaceToken(token).build()
        }
    }

    suspend fun updateDmModelFilePath(path: String,context: Context) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setDmModelFilePath(path).build()
        }
    }

    suspend fun updateNarrativeTone(tone: String,context: Context) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setNarrativeTone(tone).build()
        }
    }

    suspend fun updateGemmaTemperature(temp: String,context: Context) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setGemmaTemperature(temp).build()
        }
    }

    suspend fun updateGemmaTopK(topK: String,context: Context) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setGemmaTopK(topK).build()
        }
    }

    suspend fun updateGemmaTopP(topP: String,context: Context) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setGemmaTopP(topP).build()
        }
    }

    suspend fun updateGemmaNLen(nLen: String,context: Context) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setGemmaNLen(nLen).build()
        }
    }

    /**
     * Metodo unico per aggiornare tutti i parametri di Gemma in una sola operazione.
     */
    suspend fun updateGemmaParameters(temperature: String, topK: String, topP: String, nLen: String,context: Context) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder()
                .setGemmaTemperature(temperature)
                .setGemmaTopK(topK)
                .setGemmaTopP(topP)
                .setGemmaNLen(nLen)
                .build()
        }
    }

    suspend fun getFirstNarrativeTone(context: Context): String { // Or String? if it can be null
        return context.settingsDataStore.data
            .map { settings -> settings.narrativeTone } // Map to the String property
            .first() // Get the first (and likely only) Settings object and its narrativeTone
    }

    suspend fun getGemmaNLen(context: Context): String { // Or String? if it can be null
        return context.settingsDataStore.data
            .map { settings -> settings.gemmaNLen } // Map to the String property
            .first() // Get the first (and likely only) Settings object and its narrativeTone
    }

    suspend fun getGemmaTopP(context: Context): String { // Or String? if it can be null
        return context.settingsDataStore.data
            .map { settings -> settings.gemmaTopP } // Map to the String property
            .first() // Get the first (and likely only) Settings object and its narrativeTone
    }

    suspend fun getGemmaTopK(context: Context): String { // Or String? if it can be null
        return context.settingsDataStore.data
            .map { settings -> settings.gemmaTopK } // Map to the String property
            .first() // Get the first (and likely only) Settings object and its narrativeTone
    }

    suspend fun getGemmaTemperature(context: Context): String { // Or String? if it can be null
        return context.settingsDataStore.data
            .map { settings -> settings.gemmaTemperature } // Map to the String property
            .first() // Get the first (and likely only) Settings object and its narrativeTone
    }

    suspend fun getDmModelFilePath(context: Context): String { // Or String? if it can be null
        return context.settingsDataStore.data
            .map { settings -> settings.dmModelFilePath } // Map to the String property
            .first() // Get the first (and likely only) Settings object and its narrativeTone
    }

    suspend fun getHuggingFaceToken(context: Context): String { // Or String? if it can be null
        return context.settingsDataStore.data
            .map { settings -> settings.huggingFaceToken } // Map to the String property
            .first() // Get the first (and likely only) Settings object and its narrativeTone
    }

    suspend fun getDmModelFilePathBytes(context: Context): ByteString{ // Or String? if it can be null
        return context.settingsDataStore.data
            .map { settings -> settings.dmModelFilePathBytes } // Map to the String property
            .first() // Get the first (and likely only) Settings object and its narrativeTone
    }

}