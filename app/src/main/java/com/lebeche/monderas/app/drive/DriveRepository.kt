package com.lebeche.monderas.app.drive

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import com.lebeche.monderas.app.ui.WebUrls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class DriveItem(
    val name: String,
    val path: String,
    val isDir: Boolean,
    val size: Long = 0L
)

/** Cliente para interactuar con la API File Station de Synology. */
class DriveRepository {

    // Aceptamos certificados autofirmados para el NAS.
    private val trustAllCerts = arrayOf<TrustManager>(
        @SuppressLint("TrustAllX509TrustManager", "CustomX509TrustManager")
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    )

    private val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, trustAllCerts, SecureRandom())
    }

    private val cookieJar = object : CookieJar {
        private val cookies = mutableListOf<Cookie>()
        override fun saveFromResponse(url: HttpUrl, newCookies: List<Cookie>) {
            cookies.addAll(newCookies)
        }
        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookies
    }

    private val client = OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .cookieJar(cookieJar)
        .build()

    private var sid: String? = null
    private val baseUrl = "https://pelotxo.synology.me:5001/webapi/"

    suspend fun login(password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${baseUrl}auth.cgi?api=SYNO.API.Auth&version=3&method=login&account=${WebUrls.DRIVE_USER}&passwd=${Uri.encode(password)}&session=FileStation&format=sid"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext false
            val json = JSONObject(body)
            if (json.optBoolean("success", false)) {
                sid = json.getJSONObject("data").getString("sid")
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e("DriveRepository", "Login error", e)
        }
        return@withContext false
    }

    suspend fun listFiles(folderPath: String): List<DriveItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<DriveItem>()
        if (sid == null) return@withContext list

        try {
            val url = if (folderPath.isEmpty() || folderPath == "/") {
                "${baseUrl}entry.cgi?api=SYNO.FileStation.List&version=2&method=list_share&sid=$sid"
            } else {
                "${baseUrl}entry.cgi?api=SYNO.FileStation.List&version=2&method=list&folder_path=${Uri.encode(folderPath)}&sid=$sid"
            }
            
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext list
            val json = JSONObject(body)
            
            if (json.optBoolean("success", false)) {
                val data = json.getJSONObject("data")
                val array = if (data.has("shares")) data.getJSONArray("shares") else data.getJSONArray("files")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        DriveItem(
                            name = obj.getString("name"),
                            path = obj.getString("path"),
                            isDir = obj.getBoolean("isdir")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("DriveRepository", "List error", e)
        }
        return@withContext list.sortedWith(compareBy({ !it.isDir }, { it.name }))
    }

    suspend fun downloadFile(path: String, outputStream: OutputStream): Boolean = withContext(Dispatchers.IO) {
        if (sid == null) return@withContext false
        try {
            val url = "${baseUrl}entry.cgi?api=SYNO.FileStation.Download&version=2&method=download&path=${Uri.encode(path)}&sid=$sid"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.byteStream()?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e("DriveRepository", "Download error", e)
        }
        return@withContext false
    }

    suspend fun uploadFile(destPath: String, fileName: String, inputStream: InputStream): Boolean = withContext(Dispatchers.IO) {
        if (sid == null) return@withContext false
        try {
            val url = "${baseUrl}entry.cgi"
            val bytes = inputStream.readBytes()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api", "SYNO.FileStation.Upload")
                .addFormDataPart("version", "2")
                .addFormDataPart("method", "upload")
                .addFormDataPart("path", destPath)
                .addFormDataPart("create_parents", "true")
                .addFormDataPart("overwrite", "true")
                .addFormDataPart("file", fileName, RequestBody.create(null, bytes))
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext false
            val json = JSONObject(body)
            return@withContext json.optBoolean("success", false)
        } catch (e: Exception) {
            Log.e("DriveRepository", "Upload error", e)
            false
        }
    }
}
