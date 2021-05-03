package me.maagk.johannes.virtualpeer.fragment.exercise

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import java.util.*

class BoxBreathingFragment : Fragment(R.layout.fragment_box_breathing), Animation.AnimationListener {

    private var animationPosition = -1

    private lateinit var startButton: ImageView
    private lateinit var startAgainText: TextView
    private lateinit var countdownText: TextView
    private lateinit var breathIndicator: ImageView
    private lateinit var breathIndicatorText: TextView

    private val countdownDurationSeconds = 5
    private val breatheInDurationSeconds = 5
    private val breatheOutDurationSeconds = 5
    private val holdDurationSeconds = 5

    private val timer = Timer()
    private val timerPeriod = 250L

    companion object {
        const val TAG = "boxBreathing"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButton = view.findViewById(R.id.startButton)
        startAgainText = view.findViewById(R.id.startAgain)
        countdownText = view.findViewById(R.id.countdown)
        breathIndicator = view.findViewById(R.id.breathIndicator)
        breathIndicatorText = view.findViewById(R.id.breathIndicatorText)

        val states = Array(2) {
            IntArray(1)
        }
        states[0][0] = android.R.attr.state_pressed
        states[1][0] = android.R.attr.state_enabled

        val colors = IntArray(2)
        colors[0] = Utils.getColor(requireContext(), R.color.colorExerciseBoxBreathingGradientTo)
        colors[1] = Utils.getColor(requireContext(), R.color.colorTextContrast)

        startButton.imageTintList = ColorStateList(states, colors)

        startButton.setOnClickListener {
            val fadeOutAnimation = AlphaAnimation(1f, 0f)
            fadeOutAnimation.duration = Utils.getScaledAnimationDuration(requireContext(), 500)
            fadeOutAnimation.setAnimationListener(this)

            startButton.startAnimation(fadeOutAnimation)
            if(startAgainText.visibility == View.VISIBLE)
                startAgainText.startAnimation(fadeOutAnimation)
        }
    }

    override fun onAnimationStart(animation: Animation?) {
        animationPosition++

        when(animationPosition) {
            0 -> startButton.isClickable = false
        }
    }

    override fun onAnimationEnd(animation: Animation?) {
        when(animationPosition) {
            0 -> {
                startButton.visibility = View.GONE
                startButton.isClickable = true
                startAgainText.visibility = View.GONE

                countdownText.visibility = View.VISIBLE
                countdownText.text = countdownDurationSeconds.toString()

                val fadeInCountdownAnimation = AlphaAnimation(0f, 1f)
                fadeInCountdownAnimation.duration = Utils.getScaledAnimationDuration(requireContext(), 500)

                val fromScale = 1f
                val toScale = 0.25f
                val pivotType = Animation.RELATIVE_TO_SELF
                val pivotValue = 0.5f
                val scaleDownAnimation = ScaleAnimation(fromScale, toScale, fromScale, toScale, pivotType, pivotValue, pivotType, pivotValue)
                scaleDownAnimation.duration = countdownDurationSeconds * 1000L // not scaling this because it'll always be the same
                scaleDownAnimation.interpolator = AccelerateInterpolator(0.3f)

                val fadeOutCountdownAnimation = AlphaAnimation(1f, 0f)
                fadeOutCountdownAnimation.duration = Utils.getScaledAnimationDuration(requireContext(), 500)
                fadeOutCountdownAnimation.startOffset = Utils.getScaledAnimationDuration(requireContext(), 500) + (countdownDurationSeconds * 1000L) - 1000L

                val fadeScaleFadeAnimation = AnimationSet(false)
                fadeScaleFadeAnimation.addAnimation(fadeInCountdownAnimation)
                fadeScaleFadeAnimation.addAnimation(scaleDownAnimation)
                fadeScaleFadeAnimation.addAnimation(fadeOutCountdownAnimation)
                fadeScaleFadeAnimation.setAnimationListener(this)
                countdownText.startAnimation(fadeScaleFadeAnimation)

                timer.scheduleAtFixedRate(StartCountdownTask(), 0, timerPeriod)
            }

            1 -> {
                countdownText.visibility = View.GONE

                breathIndicator.visibility = View.VISIBLE
                breathIndicatorText.visibility = View.VISIBLE

                // showing the circle for the first time
                val fadeInAnimation = AlphaAnimation(0f, 1f)
                fadeInAnimation.duration = Utils.getScaledAnimationDuration(requireContext(), 500)

                var fromScale = 0.25f
                var toScale = 1f
                val pivotType = Animation.RELATIVE_TO_SELF
                val pivotValue = 0.5f
                val breatheInAnimation = ScaleAnimation(fromScale, toScale, fromScale, toScale, pivotType, pivotValue, pivotType, pivotValue)
                breatheInAnimation.duration = breatheInDurationSeconds * 1000L
                breatheInAnimation.interpolator = AccelerateDecelerateInterpolator()

                fromScale = 1f
                toScale = 0.25f
                val breatheOutAnimation = ScaleAnimation(fromScale, toScale, fromScale, toScale, pivotType, pivotValue, pivotType, pivotValue)
                breatheOutAnimation.duration = breatheOutDurationSeconds * 1000L
                breatheOutAnimation.interpolator = AccelerateDecelerateInterpolator()
                breatheOutAnimation.startOffset = (breatheInDurationSeconds + holdDurationSeconds) * 1000L

                val fadeOutAnimation = AlphaAnimation(1f, 0f)
                fadeOutAnimation.duration = Utils.getScaledAnimationDuration(requireContext(), 500)
                fadeOutAnimation.startOffset = breatheOutAnimation.startOffset + breatheOutAnimation.duration - fadeOutAnimation.duration

                val breathIndicatorCycleAnimation = AnimationSet(false)
                breathIndicatorCycleAnimation.addAnimation(fadeInAnimation)
                breathIndicatorCycleAnimation.addAnimation(breatheInAnimation)
                breathIndicatorCycleAnimation.addAnimation(breatheOutAnimation)
                breathIndicatorCycleAnimation.addAnimation(fadeOutAnimation)
                breathIndicatorCycleAnimation.setAnimationListener(this)

                breathIndicator.startAnimation(breathIndicatorCycleAnimation)

                val breathIndicatorTextCycleAnimation = AnimationSet(false)
                breathIndicatorTextCycleAnimation.addAnimation(fadeInAnimation)
                breathIndicatorTextCycleAnimation.addAnimation(fadeOutAnimation)

                breathIndicatorText.startAnimation(breathIndicatorTextCycleAnimation)

                timer.scheduleAtFixedRate(BoxBreathingCycleTask(), 0, timerPeriod)
            }

            2 -> {
                // one cycle is finished; resetting
                breathIndicator.visibility = View.INVISIBLE
                breathIndicatorText.visibility = View.INVISIBLE

                startButton.visibility = View.VISIBLE
                startAgainText.visibility = View.VISIBLE

                val fadeInAnimation = AlphaAnimation(0f, 1f)
                fadeInAnimation.duration = Utils.getScaledAnimationDuration(requireContext(), 500)

                startButton.startAnimation(fadeInAnimation)
                startAgainText.startAnimation(fadeInAnimation)

                animationPosition = -1
            }
        }
    }

    override fun onAnimationRepeat(animation: Animation?) {

    }

    private abstract inner class BoxBreathingTask(val totalSeconds: Int) : TimerTask() {

        protected var totalMillis = totalSeconds * 1000L
        protected var millisSinceStart = 0L
        protected var millisLeft = 0L

        protected var secondsLeft = 0L
        // this is the same as secondsLeft, just one higher
        // (example: goes from 5 to 1 instead of 4 to 0)
        protected var secondsLeftHigher = 0L

        protected val handler = Handler(Looper.getMainLooper())

        override fun run() {
            millisSinceStart += timerPeriod
            millisLeft = totalMillis - millisSinceStart

            secondsLeft = millisLeft / 1000L
            secondsLeftHigher = secondsLeft + 1

            if(millisLeft <= 0 || !isAdded)
                cancel()
        }
    }

    private inner class StartCountdownTask : BoxBreathingTask(countdownDurationSeconds) {

        override fun run() {
            super.run()

            if(isAdded) {
                handler.post {
                    countdownText.text = secondsLeftHigher.toString()
                }
            }
        }

    }

    private inner class BoxBreathingCycleTask : BoxBreathingTask(breatheInDurationSeconds + holdDurationSeconds + breatheOutDurationSeconds) {

        private val breatheInText = getString(R.string.box_breathing_breathe_in)
        private val breatheOutText = getString(R.string.box_breathing_breathe_out)
        private val holdText = getString(R.string.box_breathing_hold)

        override fun run() {
            super.run()

            val secondsSinceStart = millisSinceStart / 1000
            val secondsSinceHold = secondsSinceStart - breatheInDurationSeconds
            val secondsSinceBreatheOut = secondsSinceHold - holdDurationSeconds

            handler.post {
                breathIndicatorText.text = when {
                    millisSinceStart <= 1000L -> breatheInText
                    secondsSinceStart in 1 until breatheInDurationSeconds -> (breatheInDurationSeconds - secondsSinceStart).toString()

                    secondsSinceHold == 0L -> holdText
                    secondsSinceHold in 1 until holdDurationSeconds -> (holdDurationSeconds - secondsSinceHold).toString()

                    secondsSinceBreatheOut == 0L -> breatheOutText
                    secondsSinceBreatheOut in 1 until breatheOutDurationSeconds -> (breatheOutDurationSeconds - secondsSinceBreatheOut).toString()

                    else -> ""
                }
            }
        }

    }

}