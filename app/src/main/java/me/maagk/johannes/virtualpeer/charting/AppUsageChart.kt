package me.maagk.johannes.virtualpeer.charting

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.text.format.DateFormat
import android.util.AttributeSet
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.tracking.TrackingManager
import kotlin.math.max

class AppUsageChart @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : BarChart(context, attrs, defStyleAttr) {

    class Formatter(private val context: Context) : ValueFormatter() {

        private val dateFormat = DateFormat()

        override fun getFormattedValue(value: Float): String {
            val totalSeconds = value.toLong() / 1000
            val minutes = totalSeconds / 60 % 60
            val hours = totalSeconds / 60 / 60

            val formattedTime = if(hours == 0L) {
                context.getString(R.string.app_usage_chart_time_format_below_1h, minutes)
            } else {
                context.getString(R.string.app_usage_chart_time_format, hours, minutes)
            }

            return formattedTime
        }
    }

    private val trackingManager = TrackingManager(context)

    var maxApps = 6
        set(value) {
            field = value

            setMaxVisibleValueCount(value + 1)
            xAxis.setLabelCount(value, true)

            update()
        }

    init {
        // calling the custom setter here to initialize the things in there as well
        this::maxApps.set(maxApps)

        setDrawBarShadow(false)
        setDrawValueAboveBar(false)
        description.isEnabled = false

        setPinchZoom(false)
        setScaleEnabled(false)

        setDrawGridBackground(false)

        /*xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f*/
        xAxis.isEnabled = false

        /*axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        axisLeft.axisMinimum = 0f*/
        axisLeft.isEnabled = false
        axisRight.isEnabled = false

        axisLeft.spaceTop = 25f

        legend.isEnabled = false
        /*legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT*/

        isHighlightPerTapEnabled = false
        isHighlightFullBarEnabled = false
        isHighlightPerDragEnabled = false
        isDoubleTapToZoomEnabled = false
    }

    fun update() {
        if(data == null) {
            val dataset = BarDataSet(getCurrentChartEntries(), context.getString(R.string.app_usage_chart_dataset_label))
            dataset.valueTextColor = Utils.getColor(context, R.color.colorText)
            dataset.setDrawIcons(true)

            dataset.colors.clear()
            dataset.colors.add(Utils.getColor(context, R.color.colorPrimary))

            val data = BarData(dataset)
            data.setValueTextSize(11f)
            data.setValueFormatter(Formatter(context))
            this.data = data
        } else {
            data.dataSets[0].clear()
            for(entry in getCurrentChartEntries())
                data.dataSets[0].addEntry(entry)
        }

        notifyDataSetChanged()
    }

    private fun getCurrentChartEntries() : ArrayList<BarEntry> {
        val entries = arrayListOf<BarEntry>()
        var maxValue = 0L

        trackingManager.update()
        val mostUsedApps = trackingManager.getMostUsedApps(maxApps)
        mostUsedApps.forEachIndexed { index, app ->
            val entry = BarEntry(index.toFloat(), app.timeUsed.toFloat())
            maxValue = max(maxValue, app.timeUsed)

            val drawable = context.packageManager.getApplicationIcon(app.packageName)
            val bitmap = drawable.toBitmap()
            val sizePx = Utils.dpToPx(36, resources.displayMetrics)
            val scaledBitmap = bitmap.scale(sizePx, sizePx)

            val scaledDrawable = BitmapDrawable(resources, scaledBitmap)
            scaledDrawable.setBounds(0, -Utils.dpToPx(36, resources.displayMetrics), 0, 0)

            entry.icon = scaledDrawable

            entries.add(entry)
        }

        axisLeft.axisMaximum = maxValue.toFloat() * 1.25f

        return entries
    }

}