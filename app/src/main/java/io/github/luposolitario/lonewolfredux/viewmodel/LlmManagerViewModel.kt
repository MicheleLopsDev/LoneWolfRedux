package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import io.github.luposolitario.lonewolfredux.data.NarrativeTone
import io.github.luposolitario.lonewolfredux.datastore.ModelSettingsManager
import io.github.luposolitario.lonewolfredux.worker.ModelDownloadWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class LlmManagerViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)
    private val modelSettingsManager = ModelSettingsManager

    private val _uiState = MutableStateFlow(ModelSettingsUiState())
    val uiState: StateFlow<ModelSettingsUiState> = _uiState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    val isTokenPresent: StateFlow<Boolean> = modelSettingsManager.getSettingsFlow(application)
        .map { !it.huggingFaceToken.isNullOrBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isModelDownloaded: StateFlow<Boolean> = modelSettingsManager.getSettingsFlow(application)
        .map { settings ->
            val path = settings.dmModelFilePath
            !path.isNullOrBlank() && File(path).exists()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            modelSettingsManager.getSettingsFlow(application).collect { settings ->
                _uiState.value = ModelSettingsUiState(
                    huggingFaceToken = settings.huggingFaceToken,
                    narrativeTone = NarrativeTone.fromKey(settings.narrativeTone),
                    temperature = settings.gemmaTemperature.ifEmpty { "0.9" },
                    topK = settings.gemmaTopK.ifEmpty { "50" },
                    topP = settings.gemmaTopP.ifEmpty { "1.0" },
                    maxLength = settings.gemmaNLen.ifEmpty { "2048" }
                )
            }
        }
    }

    fun onNarrativeToneChanged(newTone: NarrativeTone) {
        viewModelScope.launch {
            modelSettingsManager.updateNarrativeTone(newTone.key, getApplication())
        }
    }

    fun onTokenChanged(newToken: String) {
        viewModelScope.launch {
            modelSettingsManager.updateHuggingFaceToken(newToken, getApplication())
        }
    }

    fun onTemperatureChanged(newTemp: String) {
        viewModelScope.launch {
            modelSettingsManager.updateGemmaTemperature(newTemp, getApplication())
        }
    }

    fun onTopKChanged(newTopK: String) {
        viewModelScope.launch {
            modelSettingsManager.updateGemmaTopK(newTopK, getApplication())
        }
    }

    fun onTopPChanged(newTopP: String) {
        viewModelScope.launch {
            modelSettingsManager.updateGemmaTopP(newTopP, getApplication())
        }
    }

    fun onMaxLengthChanged(newLength: String) {
        viewModelScope.launch {
            modelSettingsManager.updateGemmaNLen(newLength, getApplication())
        }
    }

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
                        val bytesDownloaded =
                            workInfo.progress.getLong(ModelDownloadWorker.KEY_BYTES_DOWNLOADED, 0)
                        val totalBytes = workInfo.progress.getLong(
                            ModelDownloadWorker.KEY_TOTAL_BYTES,
                            1
                        )
                        val progress = ((bytesDownloaded * 100) / totalBytes).toInt()
                        _downloadState.value = DownloadState.Downloading(progress)
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        _downloadState.value = DownloadState.Completed
                        viewModelScope.launch {
                            modelSettingsManager.updateDmModelFilePath(modelFile.absolutePath, getApplication())
                        }
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


data class ModelSettingsUiState(
    val huggingFaceToken: String = "",
    // --- MODIFICATO: narrativeTone ora è nullabile per gestire lo stato iniziale ---
    val narrativeTone: NarrativeTone? = null,
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