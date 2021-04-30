package me.maagk.johannes.virtualpeer.charting

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.useractivity.UserActivity
import me.maagk.johannes.virtualpeer.useractivity.UserActivityManager
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.math.round

class ActivityPoolChart @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : PieChart(context, attrs, defStyleAttr) {

    class Formatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if(value <= 0) "" else (round(value * 10) / 10).toString()
        }
    }

    private val userActivityManager = UserActivityManager(context)

    init {
        description.isEnabled = false
        isDrawHoleEnabled = true
        setHoleColor(Color.TRANSPARENT)
        legend.isEnabled = false

        holeRadius = Utils.dpToPx(12f, context.resources.displayMetrics)
        transparentCircleRadius = 0f

        rotationAngle = -90f
        isRotationEnabled = false
        setDrawEntryLabels(false)

        highlightValues(null)
    }

    fun update() {
        if(data == null) {
            val dataset = PieDataSet(getCurrentChartEntries(), context.getString(R.string.start_chart_dataset_label))
            dataset.sliceSpace = Utils.dpToPx(0.5f, context.resources.displayMetrics)
            dataset.setDrawValues(false)
            dataset.setDrawIcons(false)

            val activityTypes = UserActivity.Type.values()
            val colors = arrayListOf<Int>()
            for(activityType in activityTypes)
                colors.add(activityType.getColor(context))

            colors.add(Utils.getColor(context, R.color.colorChartRemainingTime))

            dataset.colors = colors

            val data = PieData(dataset)
            data.setValueFormatter(Formatter())
            data.setValueTextSize(11f)
            this.data = data
        } else {
            data.dataSet.clear()
            for(entry in getCurrentChartEntries())
                data.dataSet.addEntry(entry)
        }

        notifyDataSetChanged()
    }

    private fun getCurrentChartEntries(): ArrayList<PieEntry> {
        val todaysActivities = userActivityManager.getTodaysActivities()
        var workTotalMillis = 0L; var essentialTotalMillis = 0L; var rewardsTotalMillis = 0L
        for(activity in todaysActivities) {
            val duration = activity.getDuration(true)
            when(activity.type) {
                UserActivity.Type.POOL_WORK -> workTotalMillis += duration
                UserActivity.Type.POOL_ESSENTIAL -> essentialTotalMillis += duration
                UserActivity.Type.POOL_REWARDS -> rewardsTotalMillis += duration
            }
        }

        val workHours = TimeUnit.MILLISECONDS.toMinutes(workTotalMillis) / 60f
        val essentialHours = TimeUnit.MILLISECONDS.toMinutes(essentialTotalMillis) / 60f
        val rewardsHours = TimeUnit.MILLISECONDS.toMinutes(rewardsTotalMillis) / 60f

        val now = ZonedDateTime.now()
        val startOfToday = now.toLocalDate().atStartOfDay()
        val millisSinceStartOfToday = startOfToday.until(now, ChronoUnit.MILLIS)

        val hoursLeft = 24 - (TimeUnit.MILLISECONDS.toMinutes(millisSinceStartOfToday) / 60f)

        val entries = arrayListOf<PieEntry>()
        entries.add(PieEntry(workHours))
        entries.add(PieEntry(essentialHours))
        entries.add(PieEntry(rewardsHours))
        entries.add(PieEntry(hoursLeft))

        return entries
    }

}