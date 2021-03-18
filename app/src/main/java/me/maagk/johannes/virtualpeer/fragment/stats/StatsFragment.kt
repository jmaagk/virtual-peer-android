package me.maagk.johannes.virtualpeer.fragment.stats

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.charting.AppUsageChart
import me.maagk.johannes.virtualpeer.tracking.TrackingManager

class StatsFragment : Fragment(R.layout.fragment_stats) {

    private lateinit var appUsageChart: AppUsageChart
    private lateinit var trackingManager: TrackingManager

    companion object {
        const val TAG = "stats"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        trackingManager = TrackingManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appUsageChart = view.findViewById(R.id.appUsageChart)
        appUsageChart.trackingManager = trackingManager
        appUsageChart.maxApps = 6
    }

    override fun onResume() {
        super.onResume()

        appUsageChart.update()
    }

}