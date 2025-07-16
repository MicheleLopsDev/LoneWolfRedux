package io.github.luposolitario.lonewolfredux.navigation

import android.content.Context
import android.content.Intent
import io.github.luposolitario.lonewolfredux.ConfigurationActivity
import io.github.luposolitario.lonewolfredux.DownloadManagerActivity
import io.github.luposolitario.lonewolfredux.GameActivity
import io.github.luposolitario.lonewolfredux.LlmManagerActivity

object AppNavigator {

    fun navigateToDownloadManager(context: Context) {
        context.startActivity(Intent(context, DownloadManagerActivity::class.java))
    }

    fun navigateToLlmManager(context: Context) {
        context.startActivity(Intent(context, LlmManagerActivity::class.java))
    }

    fun navigateToConfiguration(context: Context) {
        context.startActivity(Intent(context, ConfigurationActivity::class.java))
    }

    fun navigateToGame(context: Context, bookId: Int) {
        val intent = Intent(context, GameActivity::class.java).apply {
            putExtra("BOOK_ID", bookId)
            // --- QUESTA È LA RIGA CHE RISOLVE L'ERRORE ---
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // --- FINE MODIFICA ---
        }
        context.startActivity(intent)
    }
}