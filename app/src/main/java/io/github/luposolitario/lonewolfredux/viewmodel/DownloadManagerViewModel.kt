package io.github.luposolitario.lonewolfredux.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import io.github.luposolitario.lonewolfredux.data.Book
import io.github.luposolitario.lonewolfredux.data.BookSeries
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DownloadManagerViewModel : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    init {
        loadBookList()
    }

    private fun loadBookList() {
        // Per ora, usiamo una lista statica presa dal tuo documento di progetto
        _books.value = listOf(
            Book(1, "I Signori delle Tenebre", BookSeries.KAI, "https://www.projectaon.org/en/xhtml/lw/01fftd.zip"),
            Book(2, "Traversata Infernale", BookSeries.KAI, "https://www.projectaon.org/en/xhtml/lw/02fotw/02fotw.zip"),
            // ...Aggiungi qui tutti gli altri 27 libri...
            // Per brevità, ti mostro come iniziare. Completa la lista.
            Book(6, "I Regni del Terrore", BookSeries.MAGNAKAI, "https://www.projectaon.org/en/xhtml/lw/06tkot/06tkot.zip"),
            Book(13, "La Peste dei Signori", BookSeries.GRAND_MASTER, "https://www.projectaon.org/en/xhtml/lw/13tplor/13tplor.zip"),
            Book(21, "Il Viaggio della Pietra di Luna", BookSeries.NEW_ORDER, "https://www.projectaon.org/en/xhtml/lw/21votm/21votm.zip")
            // etc...
        )
    }

    fun onPlayClicked(context: Context, bookId: Int) {
        // Più avanti, qui lanceremo l'intent per la GameActivity
        // AppNavigator.navigateToGame(context, bookId)
    }

    fun onDownloadClicked(book: Book) {
        // TODO: Logica per avviare il download con WorkManager
    }

    fun onDeleteClicked(book: Book) {
        // TODO: Logica per cancellare i file del libro
    }
}