package io.github.luposolitario.lonewolfredux.data

// Un enum per le serie dei libri, ci aiuterà a raggrupparli
enum class BookSeries(val title: String) {
    KAI("Serie Kai"),
    MAGNAKAI("Serie Magnakai"),
    GRAND_MASTER("Serie Gran Maestro"),
    NEW_ORDER("Serie Nuovo Ordine")
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