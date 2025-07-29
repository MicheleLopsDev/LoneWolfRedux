package io.github.luposolitario.lonewolfredux.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object TranslationCacheSerializer : Serializer<TranslationCache> {
    override val defaultValue: TranslationCache = TranslationCache.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): TranslationCache {
        try {
            return TranslationCache.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: TranslationCache,
        output: OutputStream
    ) = t.writeTo(output)
}