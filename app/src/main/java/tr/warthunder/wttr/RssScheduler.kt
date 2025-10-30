
package tr.warthunder.wttr

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RssScheduler {
    private const val UNIQUE_NAME = "wttr_rss_worker"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<RssWorker>(30, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
