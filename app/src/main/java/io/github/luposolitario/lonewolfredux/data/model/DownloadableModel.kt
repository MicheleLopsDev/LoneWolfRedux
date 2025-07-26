package io.github.luposolitario.lonewolfredux.data.model

data class DownloadableModel(
    val name: String,
    val size: String,
    val id: String // Usato per identificare il modello, es. "gemma-3n-E4-B-it-int4"
)
