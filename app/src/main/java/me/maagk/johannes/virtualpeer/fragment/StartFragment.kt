package me.maagk.johannes.virtualpeer.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
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

class StartFragment : Fragment(R.layout.fragment_start), FragmentActionBarTitle {

    private lateinit var userActivityManager: UserActivityManager

    private lateinit var currentActivityText: TextView
    private lateinit var chart: PieChart

    class Formatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if(value <= 0) "" else (round(value * 10) / 10).toString()
        }
    }

    companion object {
        const val TAG = "start"
    }

    override val actionBarTitle: String
        get() = getString(R.string.app_name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userActivityManager = UserActivityManager(requireContext())
    }

    override fun onResume() {
        super.onResume()

        updateChartData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentActivityText = view.findViewById(R.id.currentActivityText)
        updateCurrentActivityText()

        val changeActivityButton: Button = view.findViewById(R.id.currentActivityChange)
        val currentActivityLayout: View = view.findViewById(R.id.currentActivityLayout)
        val changeActivityLayout: View = view.findViewById(R.id.changeActivityLayout)
        changeActivityButton.setOnClickListener {
            currentActivityLayout.visibility = View.GONE
            changeActivityLayout.visibility = View.VISIBLE
        }

        val activityRadioGroup: RadioGroup = view.findViewById(R.id.radioGroup)
        activityRadioGroup.setOnCheckedChangeListener { group, id ->
            changeActivityLayout.visibility = View.GONE
            currentActivityLayout.visibility = View.VISIBLE

            val newActivityType = when(id) {
                R.id.radioButtonEssential -> UserActivity.Type.POOL_ESSENTIAL
                R.id.radioButtonRewards -> UserActivity.Type.POOL_REWARDS
                else -> UserActivity.Type.POOL_WORK
            }

            val newActivity = UserActivity(newActivityType, ZonedDateTime.now(), null)
            userActivityManager.setCurrentActivity(newActivity)

            updateCurrentActivityText()
        }

        chart = view.findViewById(R.id.startChart)

        configureChart()
    }

    private fun configureChart() {
        chart.description.isEnabled = false
        chart.isDrawHoleEnabled = true
        chart.setHoleColor(Color.TRANSPARENT)
        chart.legend.isEnabled = false

        chart.holeRadius = 55f
        chart.transparentCircleRadius = 0f

        chart.rotationAngle = -90f
        chart.isRotationEnabled = false
        chart.setDrawEntryLabels(false)

        chart.highlightValues(null)
    }

    private fun updateChartData() {
        if(chart.data == null) {
            val dataset = PieDataSet(getCurrentChartEntries(), getString(R.string.start_chart_dataset_label))
            dataset.valueTextColor = Utils.getColor(requireContext(), R.color.colorText)
            dataset.setDrawIcons(false)

            val activityTypes = UserActivity.Type.values()
            val colors = arrayListOf<Int>()
            for(activityType in activityTypes)
                colors.add(activityType.color)

            colors.add(Utils.getColor(requireContext(), R.color.colorChartRemainingTime))

            dataset.colors = colors

            val data = PieData(dataset)
            data.setValueFormatter(Formatter())
            data.setValueTextSize(11f)
            chart.data = data
        } else {
            chart.data.dataSet.clear()
            for(entry in getCurrentChartEntries())
                chart.data.dataSet.addEntry(entry)
        }
    }

    private fun updateCurrentActivityText() {
        val currentActivity = userActivityManager.getCurrentActivity()
        if(currentActivity == null) {
            currentActivityText.setText(R.string.user_activity_current_not_set)
        } else {
            val activityTextId = when(currentActivity.type) {
                UserActivity.Type.POOL_WORK -> R.string.user_activity_current_work
                UserActivity.Type.POOL_ESSENTIAL -> R.string.user_activity_current_essential
                UserActivity.Type.POOL_REWARDS -> R.string.user_activity_current_rewards
            }
            currentActivityText.text = getString(R.string.user_activity_current_display, getString(activityTextId))
        }
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