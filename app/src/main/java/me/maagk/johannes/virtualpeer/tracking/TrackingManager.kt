package me.maagk.johannes.virtualpeer.tracking

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.*

// TODO: check permission
class TrackingManager(context: Context) {

    class TrackedApp(var packageName: String, var timeUsed: Long)

    lateinit var rawStats: List<UsageStats>
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    init {
        update()
    }

    fun update() {
        val calendar = Calendar.getInstance()
        val to = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val from = calendar.timeInMillis

        rawStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, from, to)
    }

    fun getScreenTime(): Long {
        var total = 0L
        for(appStats in rawStats)
            total += appStats.totalTimeInForeground
        return total
    }

    fun getMostUsedApps(count: Int): List<TrackedApp> {
        val appList = arrayListOf<TrackedApp>()

        // grouping multiple entries of one app
        for(appStats in rawStats) {
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

        // sorting the list by their amount of time on the screen
        val sortedAppList = appList.sortedByDescending { it.timeUsed }

        return sortedAppList.subList(0, count.coerceAtMost(sortedAppList.size))
    }

}