package io.github.luposolitario.lonewolfredux.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_URL = "key_url"
        const val KEY_DESTINATION_ZIP = "key_destination_zip"
        const val KEY_UNZIP_DIR = "key_unzip_dir"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val urlString = inputData.getString(KEY_URL) ?: return@withContext Result.failure()
        val zipPath = inputData.getString(KEY_DESTINATION_ZIP) ?: return@withContext Result.failure()
        val finalUnzipDirPath = inputData.getString(KEY_UNZIP_DIR) ?: return@withContext Result.failure()

        val zipFile = File(zipPath)
        val finalUnzipDir = File(finalUnzipDirPath)
        val tempUnzipDir = File(zipFile.parentFile, "temp_${System.currentTimeMillis()}")

        try {
            Log.d("DownloadWorker", "Inizio download da: $urlString")
            downloadFileWithRedirects(urlString, zipFile)
            Log.d("DownloadWorker", "Download completato. Inizio decompressione.")

            unzip(zipFile, tempUnzipDir)
            Log.d("DownloadWorker", "Decompressione completata.")

            val contentDir = findContentDirectory(tempUnzipDir)
            if (contentDir == null) {
                Log.e("DownloadWorker", "ERRORE: La cartella del contenuto con title.htm non è stata trovata.")
                return@withContext Result.failure()
            }
            Log.d("DownloadWorker", "Trovata cartella contenuto: ${contentDir.path}")

            if (finalUnzipDir.exists()) finalUnzipDir.deleteRecursively()
            if (!contentDir.renameTo(finalUnzipDir)) {
                Log.e("DownloadWorker", "ERRORE: Impossibile rinominare la cartella del contenuto.")
                return@withContext Result.failure()
            }

            Log.d("DownloadWorker", "Lavoro completato con successo per: $finalUnzipDirPath")
            Result.success()

        } catch (e: Exception) {
            Log.e("DownloadWorker", "ERRORE CRITICO nel worker", e)
            Result.failure()
        } finally {
            zipFile.delete()
            if (tempUnzipDir.exists()) tempUnzipDir.deleteRecursively()
        }
    }

    // --- FUNZIONE DI DOWNLOAD CON LOGICA DI PROGRESSO RIPRISTINATA ---
    private suspend fun downloadFileWithRedirects(urlString: String, destinationFile: File) {
        var connection: HttpURLConnection? = null
        try {
            var currentUrl = urlString
            var redirects = 0
            val maxRedirects = 10

            while (redirects < maxRedirects) {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode in 300..399) {
                    val newUrl = connection.getHeaderField("Location")
                    if (newUrl == null) throw Exception("Redirect senza URL di destinazione.")
                    currentUrl = newUrl
                    redirects++
                    connection.disconnect()
                } else if (responseCode == HttpURLConnection.HTTP_OK) {
                    val totalSize = connection.contentLengthLong
                    var downloaded: Long = 0

                    connection.inputStream.use { input ->
                        FileOutputStream(destinationFile).use { output ->
                            val buffer = ByteArray(4 * 1024)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                downloaded += bytesRead
                                // AGGIORNA IL PROGRESSO
                                setProgress(workDataOf(
                                    "total" to totalSize,
                                    "progress" to downloaded
                                ))
                            }
                        }
                    }
                    return
                } else {
                    throw Exception("Errore HTTP: $responseCode")
                }
            }
            throw Exception("Troppi reindirizzamenti.")
        } finally {
            connection?.disconnect()
        }
    }

    // ... (unzip e findContentDirectory rimangono invariati) ...
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
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun findContentDirectory(startDir: File): File? {
        return startDir.walkTopDown().find { file ->
            file.isDirectory && file.listFiles()?.any { it.name == "title.htm" } == true
        }
    }
}