package io.github.luposolitario.lonewolfredux.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object GameSessionSerializer : Serializer<GameSession> {
    override val defaultValue: GameSession = GameSession.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): GameSession {
        try {
            return GameSession.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: GameSession, output: OutputStream) = t.writeTo(output)
}