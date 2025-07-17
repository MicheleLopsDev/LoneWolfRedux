package io.github.luposolitario.lonewolfredux.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import androidx.datastore.dataStoreFile
data class SaveSlotInfo(
    val slotId: Int,
    val lastSaved: String,
    val isEmpty: Boolean
)

object SaveGameManager {

    // Cache per memorizzare le istanze di DataStore ed evitare di ricrearle
    private val dataStoreCache = ConcurrentHashMap<String, DataStore<GameSession>>()

    /**
     * Crea o recupera dalla cache un'istanza di DataStore per un libro e uno slot specifici.
     */
    private fun getDataStoreForBook(context: Context, bookId: Int, slotId: Int): DataStore<GameSession> {
        val fileName = "book_${bookId}_session_${slotId}.pb"
        // La chiave della cache include sia il libro che lo slot
        val cacheKey = "$bookId-$slotId"

        return dataStoreCache.getOrPut(cacheKey) {
            // Usa il delegate dataStore per creare l'istanza solo se non è in cache

            DataStoreFactory.create(
                serializer = GameSessionSerializer,
                produceFile = { context.dataStoreFile(fileName) }
            )
        }
    }

    /**
     * Recupera una sessione di gioco per un libro e uno slot specifici.
     */
    suspend fun getSession(context: Context, bookId: Int, slotId: Int): GameSession {
        return getDataStoreForBook(context, bookId, slotId).data.first()
    }

    /**
     * Aggiorna una sessione di gioco per un libro e uno slot specifici.
     */
    suspend fun updateSession(context: Context, bookId: Int, slotId: Int, session: GameSession) {
        val updatedSession = session.toBuilder()
            .setLastSavedTimestamp(System.currentTimeMillis())
            .build()
        getDataStoreForBook(context, bookId, slotId).updateData { updatedSession }
    }

    /**
     * Recupera le informazioni sugli slot di salvataggio per un libro specifico.
     */
    suspend fun getSaveSlotsInfo(context: Context, bookId: Int): List<SaveSlotInfo> {
        return (1..3).map { slotId ->
            val session = getSession(context, bookId, slotId)
            val isEmpty = session.lastSavedTimestamp == 0L
            val lastSaved = if (isEmpty) {
                "Slot vuoto"
            } else {
                val date = Date(session.lastSavedTimestamp)
                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(date)
            }
            SaveSlotInfo(slotId, lastSaved, isEmpty)
        }
    }
}