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

    private lateinit var trackingManager: TrackingManager
    private lateinit var appUsageChart: AppUsageChart

    companion object {
        const val TAG = "stats"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trackingManager = TrackingManager(requireContext())

        appUsageChart = view.findViewById(R.id.appUsageChart)
    }

    override fun onResume() {
        super.onResume()

        appUsageChart.update()
    }

}