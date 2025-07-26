package io.github.luposolitario.lonewolfredux.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

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
class SettingsDataStoreManager(private val context: Context) {

    val modelSettingsFlow: Flow<ModelSettings> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(ModelSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }

    // --- METODI PER AGGIORNARE I CAMPI (CORRETTI SECONDO IL TUO .PROTO) ---

    suspend fun updateHuggingFaceToken(token: String) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setHuggingFaceToken(token).build()
        }
    }

    suspend fun updateDmModelFilePath(path: String) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setDmModelFilePath(path).build()
        }
    }

    suspend fun updateNarrativeTone(tone: String) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setNarrativeTone(tone).build()
        }
    }

    // --- METODI PER I PARAMETRI DI GEMMA (CORRETTI SECONDO IL TUO .PROTO) ---

    suspend fun updateGemmaTemperature(temp: String) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setGemmaTemperature(temp).build()
        }
    }

    suspend fun updateGemmaTopK(topK: String) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setGemmaTopK(topK).build()
        }
    }

    suspend fun updateGemmaTopP(topP: String) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setGemmaTopP(topP).build()
        }
    }

    suspend fun updateGemmaNLen(nLen: String) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder().setGemmaNLen(nLen).build()
        }
    }

    /**
     * Metodo unico per aggiornare tutti i parametri di Gemma in una sola operazione.
     */
    suspend fun updateGemmaParameters(temperature: String, topK: String, topP: String, nLen: String) {
        context.settingsDataStore.updateData { settings ->
            settings.toBuilder()
                .setGemmaTemperature(temperature)
                .setGemmaTopK(topK)
                .setGemmaTopP(topP)
                .setGemmaNLen(nLen)
                .build()
        }
    }
}