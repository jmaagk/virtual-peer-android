package me.maagk.johannes.virtualpeer.tracking

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.*

class TrackingManager(context: Context) {

    class TrackedApp(var packageName: String, var timeUsed: Long)

    val rawStats: List<UsageStats>

    init {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        val to = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val from = calendar.timeInMillis

        rawStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, from, to)
    }

    fun getMostUsedApps(count: Int): List<TrackedApp> {
        val appList = arrayListOf<TrackedApp>()

        // cleaning up the list given to us by the system
        val cleanedList = rawStats.toList()

        // grouping multiple entries of one app
        for(appStats in cleanedList) {
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