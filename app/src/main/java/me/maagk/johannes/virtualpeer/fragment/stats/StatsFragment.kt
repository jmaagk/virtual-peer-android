package me.maagk.johannes.virtualpeer.fragment.stats

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.charting.AppUsageChart
import me.maagk.johannes.virtualpeer.tracking.TrackingManager

class StatsFragment : Fragment(R.layout.fragment_stats) {

    private lateinit var screenTimeCard: StatsCard
    private lateinit var appUsageCard: ExpandableStatsCard
    private lateinit var unlockCountCard: StatsCard

    private lateinit var appUsageChartCard: MaterialCardView
    private lateinit var appUsageChart: AppUsageChart
    private lateinit var trackingManager: TrackingManager

    private open class StatsCard(val card: MaterialCardView) {
        val title: TextView = card.findViewById(R.id.cardTitle)
        val description: TextView = card.findViewById(R.id.cardDescription)
    }

    private class ExpandableStatsCard(card: MaterialCardView, val content: View) : StatsCard(card) {
        val expanded: Boolean
            get() = card.isSelected

        val expandIcon: ImageView = card.findViewById(R.id.expandIcon)

        init {
            card.setOnClickListener {
                if(expanded)
                    collapse()
                else
                    expand()
            }
        }

        fun expand() {
            animateIcon(true)
            animateContent(true)
            card.isSelected = true
        }

        fun collapse() {
            animateIcon(false)
            animateContent(false)
            card.isSelected = false
        }

        private fun animateContent(expand: Boolean) {
            if(expand) {
                val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                content.measure(wrapContentMeasureSpec, wrapContentMeasureSpec)
            }

            // initial height when collapsing, target height when expanding
            val height = content.measuredHeight

            if(expand) {
                content.layoutParams.height = 0
                content.visibility = View.VISIBLE
            }

            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    if(expand) {
                        content.layoutParams.height = if(interpolatedTime == 1f)
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        else
                            (height * interpolatedTime).toInt()
                    } else {
                        if(interpolatedTime == 1f)
                            content.visibility = View.GONE
                        else
                            content.layoutParams.height = height - (height * interpolatedTime).toInt()
                    }

                    content.requestLayout()
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }

            animation.duration = (250 * Utils.getAnimatorDurationScale(card.context)).toLong()
            content.startAnimation(animation)
        }

        private fun animateIcon(expand: Boolean) {
            val from = if(expand) 0f else 180f
            val to = if(expand) 180f else 0f

            val animation = RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            animation.fillAfter = true
            animation.duration = (250 * Utils.getAnimatorDurationScale(card.context)).toLong()

            expandIcon.startAnimation(animation)
        }
    }

    companion object {
        const val TAG = "stats"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        trackingManager = TrackingManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        screenTimeCard = StatsCard(view.findViewById(R.id.screenTimeCard))

        appUsageChartCard = view.findViewById(R.id.appUsageChartCard)
        appUsageChart = view.findViewById(R.id.appUsageChart)
        appUsageChart.trackingManager = trackingManager
        appUsageChart.maxApps = 6

        appUsageCard = ExpandableStatsCard(view.findViewById(R.id.appUsageCard), appUsageChartCard)

        unlockCountCard = StatsCard(view.findViewById(R.id.unlockCountCard))

        screenTimeCard.title.text = getString(R.string.stats_screen_time_title)

        appUsageCard.title.text = getString(R.string.stats_app_usage_title)
        appUsageCard.description.text = getString(R.string.stats_app_usage_description)

        unlockCountCard.title.text = getString(R.string.stats_screen_unlock_count_title)
    }

    override fun onResume() {
        super.onResume()

        trackingManager.update()
        appUsageChart.update()
        updateTexts()
    }

    private fun updateTexts() {
        updateScreenTimeTotalText()
        updateUnlockCountText()
    }

    private fun updateScreenTimeTotalText() {
        val screenTime = trackingManager.getScreenTime()
        val totalSeconds = screenTime / 1000
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 60 / 60

        val text = if(hours == 0L)
            getString(R.string.stats_screen_time_total_below_1h, minutes)
        else
            getString(R.string.stats_screen_time_total, hours, minutes)

        screenTimeCard.description.text = text
    }

    private fun updateUnlockCountText() {
        val unlocks = trackingManager.getUnlockCount()
        val text = if(unlocks == 1)
            getString(R.string.stats_screen_unlock_count_description_one)
        else
            getString(R.string.stats_screen_unlock_count_description, unlocks)

        unlockCountCard.description.text = text
    }

}