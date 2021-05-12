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
import me.maagk.johannes.virtualpeer.exercise.BoxBreathingExercise
import java.util.*

class BoxBreathingFragment : Fragment(R.layout.fragment_box_breathing), Animation.AnimationListener {

    private var animationPosition = -1
    private var currentLoopCount = 0

    private lateinit var startButton: ImageView
    private lateinit var startAgainText: TextView
    private lateinit var countdownText: TextView
    private lateinit var breathIndicator: ImageView
    private lateinit var breathIndicatorText: TextView

    private val countdownDurationSeconds = 5
    private val breatheInDurationSeconds = 5
    private val breatheOutDurationSeconds = 5
    private val holdDurationSeconds = 5
    private val loopCount = 20 // assuming a cycle duration of 15 seconds, this should be exactly 5 minutes

    private val timer = Timer()
    private val timerPeriod = 250L

    private var resetSwitch = false

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
            startCycle()
        }
    }

    private fun startCycle() {
        if(startButton.visibility == View.VISIBLE) {
            val fadeOutAnimation = Utils.newFadeAnimation(false, Utils.getScaledAnimationDuration(requireContext(), 500))
            fadeOutAnimation.setAnimationListener(this)

            startButton.startAnimation(fadeOutAnimation)
            if(startAgainText.visibility == View.VISIBLE)
                startAgainText.startAnimation(fadeOutAnimation)
        } else {
            animationPosition = if(currentLoopCount == 0) 0 else 1 // only showing the countdown once (on the initial start)
            onAnimationEnd(null) // passing null here is not a problem as the argument is never used
        }
    }

    override fun onAnimationStart(animation: Animation?) {
        if(resetSwitch)
            return

        animationPosition++

        when(animationPosition) {
            0 -> startButton.isClickable = false
        }
    }

    override fun onAnimationEnd(animation: Animation?) {
        if(resetSwitch)
            return

        when(animationPosition) {
            0 -> {
                startButton.visibility = View.GONE
                startButton.isClickable = true
                startAgainText.visibility = View.GONE

                countdownText.visibility = View.VISIBLE
                countdownText.text = countdownDurationSeconds.toString()

                val fadeInCountdownAnimation = Utils.newFadeAnimation(true, Utils.getScaledAnimationDuration(requireContext(), 500))

                val fromScale = 1f
                val toScale = 0.25f
                val pivotType = Animation.RELATIVE_TO_SELF
                val pivotValue = 0.5f
                val scaleDownAnimation = ScaleAnimation(fromScale, toScale, fromScale, toScale, pivotType, pivotValue, pivotType, pivotValue)
                scaleDownAnimation.duration = countdownDurationSeconds * 1000L // not scaling this because it'll always be the same
                scaleDownAnimation.interpolator = AccelerateInterpolator(0.3f)

                val fadeOutCountdownAnimation = Utils.newFadeAnimation(false, Utils.getScaledAnimationDuration(requireContext(), 500))
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
                val fadeInAnimation = Utils.newFadeAnimation(true, Utils.getScaledAnimationDuration(requireContext(), 500))

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

                val fadeOutAnimation = Utils.newFadeAnimation(false, Utils.getScaledAnimationDuration(requireContext(), 500))
                fadeOutAnimation.startOffset = breatheOutAnimation.startOffset + breatheOutAnimation.duration - fadeOutAnimation.duration

                val breathIndicatorCycleAnimation = AnimationSet(false)
                if(isFirstLoop())
                    breathIndicatorCycleAnimation.addAnimation(fadeInAnimation)
                breathIndicatorCycleAnimation.addAnimation(breatheInAnimation)
                breathIndicatorCycleAnimation.addAnimation(breatheOutAnimation)
                if(isLastLoop())
                    breathIndicatorCycleAnimation.addAnimation(fadeOutAnimation)
                breathIndicatorCycleAnimation.setAnimationListener(this)

                breathIndicator.startAnimation(breathIndicatorCycleAnimation)

                val breathIndicatorTextCycleAnimation = AnimationSet(false)
                if(isFirstLoop())
                    breathIndicatorTextCycleAnimation.addAnimation(fadeInAnimation)
                if(isLastLoop())
                breathIndicatorTextCycleAnimation.addAnimation(fadeOutAnimation)

                breathIndicatorText.startAnimation(breathIndicatorTextCycleAnimation)

                timer.scheduleAtFixedRate(BoxBreathingCycleTask(), 0, timerPeriod)
            }

            2 -> {
                // one cycle is finished; resetting
                breathIndicator.visibility = View.INVISIBLE
                breathIndicatorText.visibility = View.INVISIBLE

                animationPosition = -1

                if(isLastLoop()) {
                    reset(false)
                } else {
                    currentLoopCount++
                    startCycle()
                }
            }
        }
    }

    private fun reset(hardReset: Boolean) {
        startButton.visibility = View.VISIBLE
        startAgainText.visibility = View.VISIBLE

        if(hardReset) {
            resetSwitch = true

            startButton.clearAnimation()
            startAgainText.clearAnimation()
            countdownText.clearAnimation()
            breathIndicator.clearAnimation()
            breathIndicatorText.clearAnimation()

            countdownText.visibility = View.GONE
            breathIndicator.visibility = View.INVISIBLE
            breathIndicatorText.visibility = View.INVISIBLE

            animationPosition = -1
        } else {
            val fadeInAnimation = Utils.newFadeAnimation(true, Utils.getScaledAnimationDuration(requireContext(), 500))

            startButton.startAnimation(fadeInAnimation)
            startAgainText.startAnimation(fadeInAnimation)
        }

        currentLoopCount = 0
    }

    override fun onPause() {
        super.onPause()

        reset(true)
    }

    override fun onResume() {
        super.onResume()

        resetSwitch = false
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

            if(millisLeft <= 0 || !isAdded || resetSwitch)
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

    private fun isLastLoop() = currentLoopCount >= loopCount - 1
    private fun isFirstLoop() = currentLoopCount == 0

}