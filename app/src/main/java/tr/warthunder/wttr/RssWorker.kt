
package tr.warthunder.wttr

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL

class RssWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = applicationContext.getSharedPreferences("rss", Context.MODE_PRIVATE)
        val lastId = prefs.getString("last_id", null)

        try {
            val feedUrl = URL("https://warthunder.tr/atom")
            val conn = feedUrl.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("User-Agent", "WTTR-App/1.0")
            conn.inputStream.use { stream ->
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(stream, null)

                var event = parser.eventType
                var entryFound = false
                var currentTag: String? = null
                var entryId: String? = null
                var title: String? = null
                var link: String? = null

                while (event != XmlPullParser.END_DOCUMENT) {
                    when (event) {
                        XmlPullParser.START_TAG -> {
                            currentTag = parser.name
                            if (currentTag == "entry") {
                                entryFound = true
                                entryId = null
                                title = null
                                link = null
                            }
                            if (entryFound && currentTag == "link") {
                                val rel = parser.getAttributeValue(null, "rel")
                                val href = parser.getAttributeValue(null, "href")
                                if (rel == null || rel == "alternate") {
                                    link = href
                                }
                            }
                        }
                        XmlPullParser.TEXT -> {
                            when (currentTag) {
                                "id" -> if (entryFound) entryId = parser.text
                                "title" -> if (entryFound) title = parser.text
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "entry") {
                                // We only need the first (latest) entry
                                break
                            }
                            currentTag = null
                        }
                    }
                    parser.next().also { event = it }
                }

                if (entryId != null && entryId != lastId) {
                    // New item detected -> notify
                    val notif = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("WTTR • Yeni gönderi")
                        .setContentText(title ?: "Yeni içerik yayınlandı")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(title))
                        .setContentIntent(android.app.PendingIntent.getActivity(
                            applicationContext,
                            0,
                            android.content.Intent(applicationContext, MainActivity::class.java).apply {
                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra("open_url", link)
                            },
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= 23) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
                        ))
                        .build()
                    with(NotificationManagerCompat.from(applicationContext)) {
                        notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notif)
                    }
                    prefs.edit().putString("last_id", entryId).apply()
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
