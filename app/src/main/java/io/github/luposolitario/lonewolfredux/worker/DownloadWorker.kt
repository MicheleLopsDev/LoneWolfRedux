package io.github.luposolitario.lonewolfredux.worker // Assicurati che questo package sia corretto

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.ZipInputStream

class DownloadWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_URL = "key_url"
        const val KEY_DESTINATION = "key_destination"
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val channelId = "download_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Download in corso",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Download libro in corso")
            .setContentText("Scaricamento in background...")
            .setSmallIcon(R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(1001, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(1001, notification)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val urlString = inputData.getString(KEY_URL) ?: return@withContext Result.failure()
        val destinationPath = inputData.getString(KEY_DESTINATION) ?: return@withContext Result.failure()

        val totalDownloaded = AtomicLong(0L)
        val url = URL(urlString)

        val finalFile = File(destinationPath)
        val tempFile = File(destinationPath + ".tmp")

        // NUOVA: Cartella di destinazione per l'unzip
        val unzipDir = finalFile.parentFile ?: return@withContext Result.failure()
        if (!unzipDir.exists()) unzipDir.mkdirs()

        try {
            setForeground(createForegroundInfo())

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connect()
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure()
            }

            val totalSize = connection.contentLengthLong

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        val downloaded = totalDownloaded.addAndGet(bytesRead.toLong())
                        setProgress(
                            workDataOf(
                                "total" to totalSize,
                                "progress" to downloaded
                            )
                        )
                    }
                }
            }

            // Rinomina il file temporaneo
            if (!tempFile.renameTo(finalFile)) {
                return@withContext Result.failure()
            }

            // Decomprimi il file
            unzip(finalFile, unzipDir)

            // Elimina il file ZIP dopo la decompressione
            finalFile.delete()

            Result.success()
        } catch (e: Exception) {
            Log.e("DownloadWorker", "Errore durante il download o l'unzip", e)
            tempFile.delete()
            finalFile.delete()
            Result.failure()
        }
    }

    // Funzione per decomprimere il file
    private fun unzip(zipFile: File, targetDirectory: File) {
        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(targetDirectory, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                entry = zis.nextEntry
            }
        }
    }
}