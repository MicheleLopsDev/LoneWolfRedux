package io.github.luposolitario.lonewolfredux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import io.github.luposolitario.lonewolfredux.data.Book
import io.github.luposolitario.lonewolfredux.data.BookSeries
import io.github.luposolitario.lonewolfredux.data.DownloadStatus
import io.github.luposolitario.lonewolfredux.datastore.AppSettingsManager
import io.github.luposolitario.lonewolfredux.navigation.AppNavigator
import io.github.luposolitario.lonewolfredux.worker.DownloadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class DownloadManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()



    init {
        loadBooks()

    }



    fun onDownloadClicked(book: Book) {
        val context = getApplication<Application>().applicationContext
        val workManager = WorkManager.getInstance(context)

        val zipFile = File(context.cacheDir, "${book.id}.zip")
        val unzipDir = File(context.filesDir, "books/${book.id}")

        // --- INIZIO BLOCCO CORRETTO ---
        // Usiamo le chiavi corrette definite nel DownloadWorker
        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf(
                DownloadWorker.KEY_URL to book.downloadUrl,
                DownloadWorker.KEY_DESTINATION_ZIP to zipFile.absolutePath,
                DownloadWorker.KEY_UNZIP_DIR to unzipDir.absolutePath
            ))
            .addTag("download_${book.id}")
            .build()
        // --- FINE BLOCCO CORRETTO ---

        workManager.enqueue(workRequest)
        observeDownloadProgress(workManager, workRequest.id, book.id)
    }

    fun onDeleteClicked(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val bookDirectory = File(context.filesDir, "books/${book.id}")
            if (bookDirectory.exists()) {
                bookDirectory.deleteRecursively()
            }
            updateBookStatus(book.id, DownloadStatus.NotDownloaded)
            launch(Dispatchers.Main) { loadBooks() }
        }
    }

    fun onPlayClicked(bookId: Int) {
        val context = getApplication<Application>().applicationContext
        AppNavigator.navigateToGame(context, bookId)
    }

    private fun observeDownloadProgress(workManager: WorkManager, workId: UUID, bookId: Int) {
        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(workId).collect { workInfo ->
                if (workInfo != null) {
                    val progressData = workInfo.progress
                    val total = progressData.getLong("total", 0)
                    val progress = progressData.getLong("progress", 0)

                    val percentage = if (total > 0) ((progress * 100) / total).toInt() else 0

                    updateBookStatus(bookId, when (workInfo.state) {
                        WorkInfo.State.RUNNING -> DownloadStatus.Downloading(percentage)
                        WorkInfo.State.SUCCEEDED -> DownloadStatus.Downloaded
                        else -> DownloadStatus.NotDownloaded
                    })
                }
            }
        }
    }

    /**
     * Carica la lista dei libri e aggiorna il loro stato (scaricato e completato).
     * Questa funzione ora è il punto centrale per rinfrescare la lista.
     */
    fun loadBooks() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val allBooksSource = getAllBooks() // La nostra lista statica
            val completedIds = AppSettingsManager.getCompletedBookIds(context) // Prende gli ID dei libri completati

            val updatedBooks = allBooksSource.map { book ->
                val bookDir = File(context.filesDir, "books/${book.id}")
                val status = if (bookDir.exists() && bookDir.isDirectory) {
                    DownloadStatus.Downloaded
                } else {
                    DownloadStatus.NotDownloaded
                }
                // Crea il nuovo oggetto Book con lo stato di completamento corretto
                book.copy(
                    status = status,
                    isCompleted = completedIds.contains(book.id)
                )
            }
            _books.value = updatedBooks
        }
    }


    private fun updateBookStatus(bookId: Int, newStatus: DownloadStatus) {
        _books.update { currentBooks ->
            currentBooks.map { if (it.id == bookId) it.copy(status = newStatus) else it }
        }
    }

    // Lista completa dei libri
    // In: viewmodel/DownloadManagerViewModel.kt

// ... (il resto della classe rimane invariato)

    // Lista completa dei libri con gli URL CORRETTI
    private fun getAllBooks(): List<Book> {
        return listOf(
            Book(1, "I Signori delle Tenebre", BookSeries.KAI, "https://www.projectaon.org/en/xhtml/lw/01fftd/01fftd.zip"),
            Book(2, "Traversata infernale", BookSeries.KAI, "https://www.projectaon.org/en/xhtml/lw/02fotw/02fotw.zip"),
            Book(3, "Le Grotte di Kalte", BookSeries.KAI, "https://www.projectaon.org/en/xhtml/lw/03tcok/03tcok.zip"),
            Book(4, "L'Abisso Maledetto", BookSeries.KAI, "https://www.projectaon.org/en/xhtml/lw/04tcod/04tcod.zip"),
            Book(5, "L'Ombra sulla Sabbia", BookSeries.KAI, "https://www.projectaon.org/en/xhtml/lw/05sots/05sots.zip"),
            Book(6, "I Regni del Terrore", BookSeries.MAGNAKAI, "https://www.projectaon.org/en/xhtml/lw/06tkot/06tkot.zip"),
            Book(7, "Il Castello della Morte", BookSeries.MAGNAKAI, "https://www.projectaon.org/en/xhtml/lw/07cd/07cd.zip"),
            Book(8, "La Giungla degli Orrori", BookSeries.MAGNAKAI, "https://www.projectaon.org/en/xhtml/lw/08tjoh/08tjoh.zip"),
            Book(9, "L'Antro della Paura", BookSeries.MAGNAKAI, "https://www.projectaon.org/en/xhtml/lw/09tcof/09tcof.zip"),
            Book(10, "Le Segrete di Torgar", BookSeries.MAGNAKAI, "https://www.projectaon.org/en/xhtml/lw/10tdot/10tdot.zip"),
            Book(11, "I Prigionieri del Tempo", BookSeries.MAGNAKAI, "https://www.projectaon.org/en/xhtml/lw/11tpot/11tpot.zip"),
            Book(12, "I Signori dell'Oscurità", BookSeries.MAGNAKAI, "https://www.projectaon.org/en/xhtml/lw/12tmod/12tmod.zip"),
            Book(13, "I Signori della Peste di Ruel", BookSeries.GRAND_MASTER, "https://www.projectaon.org/en/xhtml/lw/13tplor/13tplor.zip"),
            Book(14, "I Prigionieri di Kaag", BookSeries.GRAND_MASTER, "https://www.projectaon.org/en/xhtml/lw/14tcok/14tcok.zip"),
            Book(15, "La Crociata Oscura", BookSeries.GRAND_MASTER, "https://www.projectaon.org/en/xhtml/lw/15tdc/15tdc.zip"),
            Book(16, "L'Eredità di Vashna", BookSeries.GRAND_MASTER, "https://www.projectaon.org/en/xhtml/lw/16tlov/16tlov.zip"),
            Book(17, "Il Signore della Morte di Ixia", BookSeries.GRAND_MASTER, "https://www.projectaon.org/en/xhtml/lw/17tdoi/17tdoi.zip"),
            Book(18, "L'Alba dei Draghi", BookSeries.GRAND_MASTER, "https://www.projectaon.org/en/xhtml/lw/18dotd/18dotd.zip"),
            Book(19, "La Rovina di Wolf's Bane", BookSeries.GRAND_MASTER, "https://www.projectaon.org/en/xhtml/lw/19wb/19wb.zip"),
            Book(20, "La Maledizione di Naar", BookSeries.GRAND_MASTER, "https://www.projectaon.org/en/xhtml/lw/20tcon/20tcon.zip"),
            Book(21, "Il Viaggio della Pietra di Luna", BookSeries.NEW_ORDER, "https://www.projectaon.org/en/xhtml/lw/21votm/21votm.zip"),
            Book(22, "I Bucanieri di Shadaki", BookSeries.NEW_ORDER, "https://www.projectaon.org/en/xhtml/lw/22tbos/22tbos.zip"),
            Book(23, "L'Eroe di Mydnight", BookSeries.NEW_ORDER, "https://www.projectaon.org/en/xhtml/lw/23mh/23mh.zip"),
            Book(24, "La Guerra dei Rune", BookSeries.NEW_ORDER, "https://www.projectaon.org/en/xhtml/lw/24rw/24rw.zip"),
            Book(25, "Il Sentiero del Lupo", BookSeries.NEW_ORDER, "https://www.projectaon.org/en/xhtml/lw/25totw/25totw.zip"),
            Book(26, "La Caduta della Montagna di Sangue", BookSeries.NEW_ORDER, "https://www.projectaon.org/en/xhtml/lw/26tfobm/26tfobm.zip"),
            Book(27, "Vampirium", BookSeries.NEW_ORDER, "https://www.projectaon.org/en/xhtml/lw/27v/27v.zip"),
            Book(28, "La Fame di Sejanoz", BookSeries.NEW_ORDER, "https://www.projectaon.org/en/xhtml/lw/28thos/28thos.zip"),
            Book(29, "Le Tempeste di Chai", BookSeries.NEW_ORDER, "https://www.projectaon.org/en/xhtml/lw/29tsoc/29tsoc.zip")
        )
    }
}