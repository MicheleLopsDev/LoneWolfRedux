package io.github.luposolitario.lonewolfredux.worker

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.github.luposolitario.lonewolfredux.datastore.ModelSettingsManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong

class ModelDownloadWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_URL = "key_url"
        const val KEY_DESTINATION = "key_destination"
        const val KEY_BYTES_DOWNLOADED = "key_bytes_downloaded"
        const val KEY_TOTAL_BYTES = "key_total_bytes"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "download_channel"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Download Modello In Corso")
            .setContentText("Preparazione...")
            .setSmallIcon(R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(100, 0, true)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Canale Download"
            val descriptionText = "Notifiche per i download in corso"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val urlString = inputData.getString(KEY_URL) ?: return@coroutineScope Result.failure()
        val destinationPath = inputData.getString(KEY_DESTINATION) ?: return@coroutineScope Result.failure()

        val settingsManager = ModelSettingsManager(context)
        val accessToken = settingsManager.modelSettingsFlow.first().huggingFaceToken

        if (accessToken.isEmpty()) {
            Log.e("ModelDownloadWorker", "Token di accesso non trovato.")
            return@coroutineScope Result.failure()
        }

        val finalFile = File(destinationPath)
        val tempFile = File(destinationPath + ".tmp")

        try {
            val url = URL(urlString)
            val headConnection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                setRequestProperty("Authorization", "Bearer $accessToken")
                connect()
            }

            if (headConnection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("ModelDownloadWorker", "Errore Server: ${headConnection.responseCode}")
                return@coroutineScope Result.failure()
            }

            val totalSize = headConnection.contentLengthLong
            headConnection.disconnect()

            val totalDownloaded = AtomicLong(0)
            val jobs = mutableListOf<Deferred<Unit>>()
            val numThreads = 4 // Parallelizziamo in 4 parti

            val partSize = totalSize / numThreads
            for (i in 0 until numThreads) {
                val start = i * partSize
                val end = if (i == numThreads - 1) totalSize - 1 else start + partSize - 1

                jobs.add(async(Dispatchers.IO) {
                    val connection = (url.openConnection() as HttpURLConnection).apply {
                        setRequestProperty("Range", "bytes=$start-$end")
                        setRequestProperty("Authorization", "Bearer $accessToken")
                    }
                    val input = connection.inputStream
                    val raf = RandomAccessFile(tempFile, "rw").apply { seek(start) }

                    val buffer = ByteArray(8 * 1024)
                    var bytes = input.read(buffer)
                    while (bytes != -1) {
                        raf.write(buffer, 0, bytes)
                        val downloaded = totalDownloaded.addAndGet(bytes.toLong())

                        val progress = ((downloaded * 100) / totalSize).toInt()
                        setProgress(workDataOf(KEY_BYTES_DOWNLOADED to downloaded, KEY_TOTAL_BYTES to totalSize))

                        bytes = input.read(buffer)
                    }
                    input.close()
                    raf.close()
                })
            }
            jobs.awaitAll()

            finalFile.delete()
            if (!tempFile.renameTo(finalFile)) {
                throw IOException("Impossibile rinominare il file temporaneo.")
            }

            settingsManager.updateDmModelFilePath(destinationPath)
            Result.success()
        } catch (e: Exception) {
            Log.e("ModelDownloadWorker", "Errore durante il download: ${e.message}", e)
            tempFile.delete()
            Result.failure()
        }
    }
}