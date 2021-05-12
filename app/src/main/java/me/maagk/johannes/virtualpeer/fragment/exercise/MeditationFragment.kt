package me.maagk.johannes.virtualpeer.fragment.exercise

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.exercise.ExerciseStorage
import me.maagk.johannes.virtualpeer.exercise.MeditationExercise
import me.maagk.johannes.virtualpeer.view.EyeView
import java.util.*

class MeditationFragment : Fragment(R.layout.fragment_meditation), Animation.AnimationListener {

    companion object {
        const val TAG = "meditation"
    }

    private lateinit var meditationStartLayout: ViewGroup
    private lateinit var eyeView: EyeView
    private lateinit var letsGoText: TextView

    private lateinit var countdownLayout: ViewGroup
    private lateinit var closeYourEyesText: TextView
    private lateinit var countdownText: TextView

    private var animationPosition = -1

    private val countdownTimerPeriod = 250L
    private val countdownDurationSeconds = 5
    private val timer = Timer()

    private lateinit var exerciseStorage: ExerciseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exerciseStorage = ExerciseStorage(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        meditationStartLayout = view.findViewById(R.id.meditationStartLayout)
        val durationSpinner: Spinner = view.findViewById(R.id.durationSpinner)
        val startButton: ImageView = view.findViewById(R.id.startButton)
        eyeView = view.findViewById(R.id.eyeView)
        letsGoText = view.findViewById(R.id.letsGoText)
        countdownLayout = view.findViewById(R.id.countdownLayout)
        closeYourEyesText = view.findViewById(R.id.closeYourEyesText)
        countdownText = view.findViewById(R.id.countdown)

        val durations = requireContext().resources.getStringArray(R.array.meditation_durations)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.view_duration_spinner_item, android.R.id.text1, durations)

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        durationSpinner.adapter = arrayAdapter

        val states = Array(2) {
            IntArray(1)
        }
        states[0][0] = android.R.attr.state_pressed
        states[1][0] = android.R.attr.state_enabled

        val colors = IntArray(2)
        colors[0] = Utils.getColor(requireContext(), R.color.colorExerciseMeditationGradientFrom)
        colors[1] = Utils.getColor(requireContext(), R.color.colorTextDark)

        startButton.imageTintList = ColorStateList(states, colors)

        startButton.setOnClickListener {
            exerciseStorage.refresh()
            exerciseStorage.notifyExerciseStart<MeditationExercise>()
            exerciseStorage.save()

            val fadeOutAnimation = Utils.newFadeAnimation(false, Utils.getScaledAnimationDuration(requireContext(), 500))
            fadeOutAnimation.setAnimationListener(this)

            meditationStartLayout.startAnimation(fadeOutAnimation)
        }
    }

    override fun onAnimationStart(p0: Animation?) {
        animationPosition++
    }

    override fun onAnimationEnd(p0: Animation?) {
        when(animationPosition) {

            // the initial layout has faded out
            0 -> {
                meditationStartLayout.visibility = View.GONE

                letsGoText.visibility = View.VISIBLE

                val fadeScaleFadeAnimation = AnimationSet(false)

                val fadeInAnimation = Utils.newFadeAnimation(true, Utils.getScaledAnimationDuration(requireContext(), 500))
                fadeScaleFadeAnimation.addAnimation(fadeInAnimation)

                val fromScale = 1f
                val toScale = 0.5f
                val pivotType = Animation.RELATIVE_TO_SELF
                val pivotValue = 0.5f
                val scaleDownAnimation = ScaleAnimation(fromScale, toScale, fromScale, toScale, pivotType, pivotValue, pivotType, pivotValue)
                scaleDownAnimation.interpolator = LinearInterpolator()
                scaleDownAnimation.duration = Utils.getScaledAnimationDuration(requireContext(), 2000)
                fadeScaleFadeAnimation.addAnimation(scaleDownAnimation)

                val fadeOutDuration = Utils.getScaledAnimationDuration(requireContext(), 500)
                val fadeOutAnimation = Utils.newFadeAnimation(false, fadeOutDuration)
                fadeOutAnimation.startOffset = Utils.getScaledAnimationDuration(requireContext(), scaleDownAnimation.duration - fadeOutDuration)
                fadeScaleFadeAnimation.addAnimation(fadeOutAnimation)

                fadeScaleFadeAnimation.setAnimationListener(this)
                fadeScaleFadeAnimation.startOffset = Utils.getScaledAnimationDuration(requireContext(), 250)
                letsGoText.startAnimation(fadeScaleFadeAnimation)
            }

            // the "let's go" text has been scaled down and has faded out
            1 -> {
                letsGoText.visibility = View.GONE

                countdownLayout.visibility = View.VISIBLE

                val fadeInAnimation = Utils.newFadeAnimation(true, Utils.getScaledAnimationDuration(requireContext(), 500))
                countdownLayout.startAnimation(fadeInAnimation)

                val fadeOutAnimation = Utils.newFadeAnimation(false, Utils.getScaledAnimationDuration(requireContext(), 500))
                fadeOutAnimation.startOffset = Utils.getScaledAnimationDuration(requireContext(), 1000)
                fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {}

                    override fun onAnimationEnd(p0: Animation?) {
                        closeYourEyesText.visibility = View.INVISIBLE
                    }

                    override fun onAnimationRepeat(p0: Animation?) {}
                })
                closeYourEyesText.startAnimation(fadeOutAnimation)

                val scaleDownFadeAnimation = AnimationSet(false)

                val fromScale = 1f
                val toScale = 0.25f
                val pivotType = Animation.RELATIVE_TO_SELF
                val pivotValue = 0.5f
                val scaleDownAnimation = ScaleAnimation(fromScale, toScale, fromScale, toScale, pivotType, pivotValue, pivotType, pivotValue)
                scaleDownAnimation.interpolator = LinearInterpolator()
                scaleDownAnimation.duration = countdownDurationSeconds * 1000L
                scaleDownFadeAnimation.addAnimation(scaleDownAnimation)

                val fadeOutDuration = Utils.getScaledAnimationDuration(requireContext(), 500)
                val fadeOutCountdown = Utils.newFadeAnimation(false, fadeOutDuration)
                fadeOutCountdown.startOffset = scaleDownAnimation.duration - fadeOutDuration
                scaleDownFadeAnimation.addAnimation(fadeOutCountdown)

                scaleDownFadeAnimation.setAnimationListener(this)
                countdownText.startAnimation(scaleDownFadeAnimation)

                timer.scheduleAtFixedRate(CountdownTask(), 0, countdownTimerPeriod)
            }

            // the countdown has finished
            2 -> {
                countdownText.visibility = View.INVISIBLE

                eyeView.visibility = View.VISIBLE

                val fadeInAnimation = Utils.newFadeAnimation(true, Utils.getScaledAnimationDuration(requireContext(), 500))
                fadeInAnimation.setAnimationListener(this)
                fadeInAnimation.startOffset = Utils.getScaledAnimationDuration(requireContext(), 250)

                eyeView.startAnimation(fadeInAnimation)
            }

            // the eye has faded in
            3 -> {
                val duration = Utils.getScaledAnimationDuration(requireContext(), 1500)
                eyeView.close(duration)

                val emptyAnimation = AlphaAnimation(1f, 1f)
                emptyAnimation.duration = duration
                emptyAnimation.setAnimationListener(this)
                eyeView.startAnimation(emptyAnimation)
            }

            // the eye is closed
            4 -> {
                // TODO: play sounds
                // TODO: use time set by user
            }
        }
    }

    override fun onAnimationRepeat(p0: Animation?) {

    }

    private inner class CountdownTask : TimerTask() {

        private val handler = Handler(Looper.getMainLooper())

        private var millisSinceStart = 0L
        private var millisLeft = 0L
        private val totalMillis = countdownDurationSeconds * 1000L

        override fun run() {
            millisSinceStart += countdownTimerPeriod
            millisLeft = totalMillis - millisSinceStart

            val secondsLeft = millisLeft / 1000L
            handler.post {
                countdownText.text = (secondsLeft + 1).toString()
            }

            if(millisLeft <= 0 || !isAdded)
                cancel()
        }

    }

    override fun onPause() {
        super.onPause()

        exerciseStorage.refresh()
        exerciseStorage.notifyExerciseEnd<MeditationExercise>()
        exerciseStorage.save()
    }

}