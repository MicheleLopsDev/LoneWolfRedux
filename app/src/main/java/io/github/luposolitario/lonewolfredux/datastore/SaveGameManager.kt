package io.github.luposolitario.lonewolfredux.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Definiamo i DataStore per ogni slot
private val Context.gameSessionSlot1: DataStore<GameSession> by dataStore("game_session_1.pb", GameSessionSerializer)
private val Context.gameSessionSlot2: DataStore<GameSession> by dataStore("game_session_2.pb", GameSessionSerializer)
private val Context.gameSessionSlot3: DataStore<GameSession> by dataStore("game_session_3.pb", GameSessionSerializer)

data class SaveSlotInfo(
    val slotId: Int,
    val lastSaved: String,
    val isEmpty: Boolean
)

object SaveGameManager {

    private fun getDatastoreForSlot(context: Context, slotId: Int): DataStore<GameSession> {
        return when (slotId) {
            1 -> context.gameSessionSlot1
            2 -> context.gameSessionSlot2
            3 -> context.gameSessionSlot3
            else -> throw IllegalArgumentException("Invalid slot ID")
        }
    }

    suspend fun getSession(context: Context, slotId: Int): GameSession {
        return getDatastoreForSlot(context, slotId).data.first()
    }

    suspend fun updateSession(context: Context, slotId: Int, session: GameSession) {
        // Aggiorniamo il timestamp prima di salvare
        val updatedSession = session.toBuilder()
            .setLastSavedTimestamp(System.currentTimeMillis())
            .build()
        getDatastoreForSlot(context, slotId).updateData { updatedSession }
    }

    suspend fun getSaveSlotsInfo(context: Context): List<SaveSlotInfo> {
        return (1..3).map { slotId ->
            val session = getSession(context, slotId)
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