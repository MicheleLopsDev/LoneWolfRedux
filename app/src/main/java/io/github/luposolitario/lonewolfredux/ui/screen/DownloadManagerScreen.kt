package io.github.luposolitario.lonewolfredux.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
// --- IMPORT NUOVE ICONE ---
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
// -------------------------
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            groupedBooks.forEach { (series, booksInSeries) ->
                item {
                    Text(
                        text = series.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                    )
                }
                items(booksInSeries) { book ->
                    BookRow(
                        book = book,
                        onPlay = { viewModel.onPlayClicked(book.id) },
                        onDownload = { viewModel.onDownloadClicked(book) },
                        onDelete = { viewModel.onDeleteClicked(book) }
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
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Titolo e icona di completamento (invariati)
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Mostra l'icona della serie
            Icon(
                painter = painterResource(id = book.series.iconResId),
                contentDescription = "Icona Serie ${book.series.title}",
                modifier = Modifier.size(24.dp), // Dimensione dell'icona
                tint = Color.Unspecified // IMPORTANTE: per usare i colori originali del PNG
            )

            Spacer(modifier = Modifier.width(12.dp)) // Spazio tra icona e testo

            Text(text = "${book.id}. ${book.title}")
            if (book.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Completato",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // --- BLOCCO MODIFICATO CON ICONE ---
        // Contenitore per i pulsanti di azione
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (val status = book.status) {
                is DownloadStatus.NotDownloaded -> {
                    IconButton(onClick = onDownload) {
                        Icon(Icons.Default.Download, contentDescription = "Scarica")
                    }
                }
                is DownloadStatus.Downloading -> {
                    // Mostra un indicatore di progresso
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                is DownloadStatus.Downloaded -> {
                    IconButton(onClick = onPlay) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Gioca", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        // --- FINE BLOCCO MODIFICATO ---
    }
}