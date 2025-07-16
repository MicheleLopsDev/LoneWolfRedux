package io.github.luposolitario.lonewolfredux.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.luposolitario.lonewolfredux.ui.composables.BookWebView
import io.github.luposolitario.lonewolfredux.ui.composables.SheetWebView
import io.github.luposolitario.lonewolfredux.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel, onClose: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val bookUrl by viewModel.bookUrl.collectAsState()
    val sheetUrl by viewModel.sheetUrl.collectAsState()
    val jsToRun by viewModel.jsToRunInSheet.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.9f)) {
                SheetWebView(
                    modifier = Modifier.fillMaxSize(),
                    url = sheetUrl,
                    viewModel = viewModel,
                    jsToRun = jsToRun,
                    onJsExecuted = { viewModel.onJsExecuted() }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Lone Wolf") },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, "Chiudi")
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Person, "Scheda Azione")
                        }
                    }
                )
            }
        ) { padding ->
            BookWebView(
                modifier = Modifier.padding(padding).fillMaxSize(),
                url = bookUrl,
                onNewUrl = { viewModel.onNewUrl(it) }
            )
        }
    }
}