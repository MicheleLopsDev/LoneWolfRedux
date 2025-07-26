package io.github.luposolitario.lonewolfredux.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore

private const val DATA_STORE_FILE_NAME = "model_settings.pb"

val Context.modelSettingsDataStore: DataStore<ModelSettings> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = ModelSettingsSerializer
)
