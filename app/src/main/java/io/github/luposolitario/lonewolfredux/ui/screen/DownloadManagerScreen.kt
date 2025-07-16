package io.github.luposolitario.lonewolfredux.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.luposolitario.lonewolfredux.data.Book
import io.github.luposolitario.lonewolfredux.data.DownloadStatus
import io.github.luposolitario.lonewolfredux.viewmodel.DownloadManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerScreen(viewModel: DownloadManagerViewModel) {
    val books by viewModel.books.collectAsState()
    val groupedBooks = books.groupBy { it.series }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Libreria - Lupo Solitario") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).padding(horizontal = 8.dp)
        ) {
            groupedBooks.forEach { (series, booksInSeries) ->
                item {
                    Text(
                        text = series.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                items(booksInSeries) { book ->
                    BookRow(
                        book = book,
                        // --- INIZIO BLOCCO CORRETTO ---
                        // Rimuoviamo il 'context' dalle chiamate
                        onPlay = { viewModel.onPlayClicked(book.id) },
                        onDownload = { viewModel.onDownloadClicked(book) },
                        onDelete = { viewModel.onDeleteClicked(book) }
                        // --- FINE BLOCCO CORRETTO ---
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun BookRow(
    book: Book,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${book.id}. ${book.title}",
            modifier = Modifier.weight(1f)
        )
        when (val status = book.status) {
            is DownloadStatus.NotDownloaded -> Button(onClick = onDownload) { Text("Scarica") }
            is DownloadStatus.Downloading -> Text("Scaricando: ${status.progress}%")
            is DownloadStatus.Downloaded -> {
                Button(onClick = onPlay) { Text("Gioca") }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onDelete) { Text("Elimina") }
            }
        }
    }
}