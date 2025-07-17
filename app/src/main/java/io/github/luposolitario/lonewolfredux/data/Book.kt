package io.github.luposolitario.lonewolfredux.data

import androidx.annotation.DrawableRes
import io.github.luposolitario.lonewolfredux.R

// Aggiungi un parametro @DrawableRes al costruttore dell'enum
enum class BookSeries(val title: String, @DrawableRes val iconResId: Int) {
    KAI("Serie Kai", R.drawable.green),
    MAGNAKAI("Serie Magnakai", R.drawable.blue),
    GRAND_MASTER("Serie Gran Maestro", R.drawable.red),
    NEW_ORDER("Serie Nuovo Ordine", R.drawable.purple)
}

// Uno stato per sapere se un libro è scaricato o meno
sealed class DownloadStatus {
    object NotDownloaded : DownloadStatus()
    data class Downloading(val progress: Int) : DownloadStatus()
    object Downloaded : DownloadStatus()
}

// La classe principale che rappresenta un singolo libro
data class Book(
    val id: Int,
    val title: String,
    val series: BookSeries,
    val downloadUrl: String,
    val status: DownloadStatus = DownloadStatus.NotDownloaded,
    val isCompleted: Boolean = false
)