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
            // Aggiungiamo l'ID del libro come "extra" all'Intent.
            // La GameActivity lo userà per sapere quale libro caricare.
            putExtra("BOOK_ID", bookId)
        }
        context.startActivity(intent)
    }
}