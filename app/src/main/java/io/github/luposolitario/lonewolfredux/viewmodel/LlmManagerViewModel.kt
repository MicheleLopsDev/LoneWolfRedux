package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import io.github.luposolitario.lonewolfredux.datastore.ModelSettingsManager
import io.github.luposolitario.lonewolfredux.worker.ModelDownloadWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class LlmManagerViewModel(
    application: Application,
    private val modelSettingsManager: ModelSettingsManager
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    val uiState: StateFlow<ModelSettingsUiState> = modelSettingsManager.modelSettingsFlow
        .map { settings ->
            ModelSettingsUiState(
                huggingFaceToken = settings.huggingFaceToken,
                temperature = settings.gemmaTemperature.ifEmpty { "0.9" },
                topK = settings.gemmaTopK.ifEmpty { "50" },
                topP = settings.gemmaTopP.ifEmpty { "1.0" },
                maxLength = settings.gemmaNLen.ifEmpty { "2048" }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ModelSettingsUiState()
        )

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    val isModelDownloaded: StateFlow<Boolean> = modelSettingsManager.modelSettingsFlow
        .map { !it.dmModelFilePath.isNullOrEmpty() && File(it.dmModelFilePath).exists() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- FUNZIONI CHIAMATE DALLA UI ---

    fun onTokenChanged(newToken: String) {
        viewModelScope.launch {
            modelSettingsManager.updateHuggingFaceToken(newToken)
        }
    }

    fun onTemperatureChanged(newTemp: String) {
        viewModelScope.launch {
            modelSettingsManager.updateGemmaTemperature(newTemp)
        }
    }

    fun onTopKChanged(newTopK: String) {
        viewModelScope.launch {
            modelSettingsManager.updateGemmaTopK(newTopK)
        }
    }

    fun onTopPChanged(newTopP: String) {
        viewModelScope.launch {
            modelSettingsManager.updateGemmaTopP(newTopP)
        }
    }

    fun onMaxLengthChanged(newLength: String) {
        viewModelScope.launch {
            modelSettingsManager.updateGemmaNLen(newLength)
        }
    }

    // Dentro la funzione startModelDownload()
    fun startModelDownload() {
        val dmDirectory = getApplication<Application>().filesDir
        val modelFile = File(dmDirectory, "gemma-3n-E4B-it-int4.task")
        val modelUrl =
            "https://huggingface.co/google/gemma-3n-E4B-it-litert-preview/resolve/main/gemma-3n-E4B-it-int4.task?download=true"

        val inputData = workDataOf(
            ModelDownloadWorker.KEY_URL to modelUrl,
            ModelDownloadWorker.KEY_DESTINATION to modelFile.absolutePath
        )

        val downloadRequest = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setInputData(inputData)
            .build()

        workManager.enqueue(downloadRequest)

        workManager.getWorkInfoByIdLiveData(downloadRequest.id).observeForever { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        // --- INIZIO BLOCCO MODIFICATO ---
                        // Calcoliamo la percentuale dai byte
                        val bytesDownloaded =
                            workInfo.progress.getLong(ModelDownloadWorker.KEY_BYTES_DOWNLOADED, 0)
                        val totalBytes = workInfo.progress.getLong(
                            ModelDownloadWorker.KEY_TOTAL_BYTES,
                            1
                        ) // Evita divisione per zero
                        val progress = ((bytesDownloaded * 100) / totalBytes).toInt()
                        _downloadState.value = DownloadState.Downloading(progress)
                        // --- FINE BLOCCO MODIFICATO ---
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        _downloadState.value = DownloadState.Completed
                    }

                    WorkInfo.State.FAILED -> {
                        _downloadState.value = DownloadState.Failed("Download fallito")
                    }

                    else -> {}
                }
            }
        }
    }
}
// --- CLASSI DI STATO PER LA UI ---

data class ModelSettingsUiState(
    val huggingFaceToken: String = "",
    val temperature: String = "0.9",
    val topK: String = "50",
    val topP: String = "1.0",
    val maxLength: String = "2048"
)

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    object Completed : DownloadState()
    data class Failed(val error: String) : DownloadState()
}