package me.maagk.johannes.virtualpeer.tracking

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class TrackingManager(private val context: Context, update: Boolean = true) {

    class TrackedApp(var packageName: String, var timeUsed: Long)

    lateinit var rawUsageStats: List<UsageStats>
    lateinit var rawEventData: UsageEvents
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private var from = -1L
    private var to = -1L

    // this is saved because the raw stats are only accessible once (hasNextEvent())
    private var unlockCount = 0

    init {
        if(update)
            update()
    }

    fun update() = update(from, to)

    fun update(from: Long, to: Long = System.currentTimeMillis()) {
        this.from = from
        this.to = to

        if(this.from == -1L || this.to == -1L) {
            val calendar = Calendar.getInstance()

            if(this.to == -1L)
                this.to = calendar.timeInMillis

            if(this.from == -1L) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                this.from = calendar.timeInMillis
            }
        }

        // getting the raw data from Android
        rawUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, this.from, this.to)
        rawEventData = usageStatsManager.queryEvents(this.from, this.to)
    }

    fun getApps(): List<TrackedApp> {
        val appList = arrayListOf<TrackedApp>()

        // grouping multiple entries of one app
        for(appStats in rawUsageStats) {
            // only adding apps that were actually in the foreground
            if(appStats.totalTimeInForeground > 0L) {
                val packageName = appStats.packageName

                // checking if the list already contains this package
                var index = -1
                appList.forEachIndexed start@ { i, item ->
                    if(item.packageName == packageName) {
                        index = i
                        return@start
                    }
                }

                // either adding the app to the list or adding to its total time used
                if(index == -1) {
                    val app = TrackedApp(packageName, appStats.totalTimeInForeground)
                    appList.add(app)
                } else {
                    appList[index].timeUsed += appStats.totalTimeInForeground
                }
            }
        }

        return appList
    }

    fun getMostUsedApps(count: Int): List<TrackedApp> {
        val appList = getApps()

        // sorting the list by their amount of time on the screen
        val sortedAppList = appList.sortedByDescending { it.timeUsed }

        return sortedAppList.subList(0, count.coerceAtMost(sortedAppList.size))
    }

    fun getScreenTime(): Long {
        var total = 0L
        for(appStats in rawUsageStats)
            total += appStats.totalTimeInForeground
        return total
    }

    fun getUnlockCount(): Int {
        if(rawEventData.hasNextEvent()) {
            unlockCount = 0

            while(rawEventData.hasNextEvent()) {
                val event = UsageEvents.Event()
                rawEventData.getNextEvent(event)

                if(event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN)
                    unlockCount++
            }
        }

        return unlockCount
    }

    fun isUsageStatsPermissionGranted(): Boolean {
        val appOpsManager = context.getSystemService(AppCompatActivity.APP_OPS_SERVICE) as AppOpsManager

        val mode = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow("android:get_usage_stats", Process.myUid(), context.opPackageName)
        } else {
            appOpsManager.checkOpNoThrow("android:get_usage_stats", Process.myUid(), context.packageName)
        }

        return mode == AppOpsManager.MODE_ALLOWED
    }

}