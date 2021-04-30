package me.maagk.johannes.virtualpeer.fragment

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.maagk.johannes.virtualpeer.activity.MainActivity
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.Utils.Companion.setTransitions
import me.maagk.johannes.virtualpeer.charting.ActivityPoolChart
import me.maagk.johannes.virtualpeer.chat.*
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment
import me.maagk.johannes.virtualpeer.fragment.exercise.EisenhowerMatrixFragment
import me.maagk.johannes.virtualpeer.survey.question.*
import me.maagk.johannes.virtualpeer.useractivity.UserActivity
import me.maagk.johannes.virtualpeer.useractivity.UserActivityManager
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class StartFragment : Fragment(R.layout.fragment_start), FragmentActionBarTitle, ChatFragment.OnMessageSentListener, OnChartValueSelectedListener {

    private lateinit var userActivityManager: UserActivityManager

    private lateinit var currentActivityText: TextView
    private lateinit var chart: ActivityPoolChart
    private lateinit var userGreeting: TextView

    private lateinit var rootLayout: ViewGroup
    private lateinit var mainLayout: ViewGroup
    private lateinit var headerLayout: ViewGroup
    private lateinit var expandCollapseIcon: ImageView

    private lateinit var activityAreaInfoLayout: ViewGroup
    private lateinit var activityAreaNameText: TextView
    private lateinit var timeSpentText: TextView
    private lateinit var sentimentText: TextView
    private lateinit var successText: TextView

    private lateinit var pref: SharedPreferences

    companion object {
        const val TAG = "start"
    }

    override val actionBarTitle: String
        get() = getString(R.string.app_name)

    private inner class HeaderLayoutHandler : ViewTreeObserver.OnGlobalLayoutListener, ViewTreeObserver.OnDrawListener {

        var maxDistanceFromTop = -1
        val maxCornerRadius = Utils.dpToPx(15f, requireContext().resources.displayMetrics)

        val paint = Paint()

        init {
            paint.isAntiAlias = true
            paint.color = Utils.getColor(requireContext(), R.color.colorBackground)
        }

        fun onResume() {
            headerLayout.viewTreeObserver.addOnGlobalLayoutListener(this)
        }

        fun onPause() {
            headerLayout.viewTreeObserver.removeOnDrawListener(this)
        }

        override fun onGlobalLayout() {
            headerLayout.viewTreeObserver.addOnDrawListener(this)
            headerLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }

        override fun onDraw() {
            // adjusting the corner radius of the main layout
            val distanceFromTop = mainLayout.top - rootLayout.top
            maxDistanceFromTop = max(distanceFromTop, maxDistanceFromTop)

            val expansionValue = distanceFromTop.toFloat() / maxDistanceFromTop.toFloat()
            if(expansionValue.isNaN())
                return

            val cornerRadius = expansionValue * maxCornerRadius

            val bitmap = Bitmap.createBitmap(headerLayout.width, headerLayout.height, Bitmap.Config.ARGB_8888)
            val drawable = BitmapDrawable(requireContext().resources, bitmap)
            val canvas = Canvas(bitmap)
            headerLayout.background = drawable

            val width = headerLayout.width.toFloat()
            val height = headerLayout.height.toFloat()

            canvas.drawRoundRect(0f, 0f, width, height, cornerRadius, cornerRadius, paint)
            canvas.drawRect(0f, height / 2, width, height, paint)

            // adjusting the rotation of the icon
            expandCollapseIcon.rotation = 180f * expansionValue
        }
    }

    private lateinit var headerLayoutHandler: HeaderLayoutHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userActivityManager = UserActivityManager(requireContext())
    }

    override fun onPause() {
        super.onPause()

        headerLayoutHandler.onPause()
    }

    override fun onResume() {
        super.onResume()

        // this is here because the user might have changed their name; this will respect that change
        val username = pref.getString(getString(R.string.pref_name), null)
        if(username != null)
            userGreeting.text = getString(R.string.start_user_greeting, username)
        else
            userGreeting.visibility = View.GONE

        headerLayoutHandler.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        currentActivityText = view.findViewById(R.id.currentActivityText)
        updateCurrentActivityText()

        userGreeting = view.findViewById(R.id.userGreeting)
        val changeActivityButton: Button = view.findViewById(R.id.currentActivityChange)
        val currentActivityLayout: View = view.findViewById(R.id.currentActivityLayout)
        val changeActivityLayout: View = view.findViewById(R.id.changeActivityLayout)
        val activityRadioGroup: RadioGroup = view.findViewById(R.id.radioGroup)

        changeActivityButton.setOnClickListener {
            currentActivityLayout.visibility = View.GONE
            changeActivityLayout.visibility = View.VISIBLE
        }

        activityRadioGroup.setOnCheckedChangeListener start@ { group, id ->
            if(id == -1)
                return@start

            changeActivityLayout.visibility = View.GONE
            currentActivityLayout.visibility = View.VISIBLE

            val newActivityType = when(id) {
                R.id.radioButtonEssential -> UserActivity.Type.POOL_ESSENTIAL
                R.id.radioButtonRewards -> UserActivity.Type.POOL_REWARDS
                else -> UserActivity.Type.POOL_WORK
            }

            val newActivity = UserActivity(newActivityType, ZonedDateTime.now(), null)
            userActivityManager.setCurrentActivity(newActivity)

            if(isAdded) {
                val questionType = (0..4).random()
                val questionMessage = when(questionType) {
                    0 -> TextInputQuestionMessage(Question.getExampleTextInputQuestion(requireContext()))
                    1 -> EmojiQuestionMessage(Question.getExampleEmojiQuestion(requireContext()))
                    2 -> SliderQuestionMessage(Question.getExampleSliderQuestion(requireContext()))
                    3 -> MultipleChoiceQuestionMessage(Question.getExampleMultipleChoiceQuestion(requireContext()))
                    else -> ChoosePictureQuestionMessage(Question.getExampleChoosePictureQuestion(requireContext()))
                }
                questionMessage.question.tag = "activity_rating"

                val mainActivity = activity as MainActivity
                mainActivity.queueMessage(questionMessage)
            }

            updateCurrentActivityText()
            group.clearCheck()
        }

        chart = view.findViewById(R.id.startChart)
        chart.setOnChartValueSelectedListener(this)
        chart.viewTreeObserver.addOnGlobalLayoutListener {
            // having this here instead of in onResume causes this chart to be updated whenever
            // a layout change happens; this should always keep this up-to-date
            chart.update()

            val highlighted = chart.highlighted

            if(highlighted == null || highlighted.isEmpty()) {
                for(i in 0 until chart.data.dataSet.entryCount) {
                    val entry = chart.data.dataSet.getEntryForIndex(i)

                    if(entry.y > 0) {
                        chart.highlightValue(i.toFloat(), 0)
                        break
                    }
                }
            } else {
                val highlight = highlighted[0]
                val entry = chart.data.getEntryForHighlight(highlight)

                // calling the listener to refresh the values
                onValueSelected(entry, highlight)
            }
        }

        val eisenhowerMatrixButton: CardView = view.findViewById(R.id.eisenhowerMatrixButton)
        eisenhowerMatrixButton.setOnClickListener {
            val eisenhowerMatrixFragment = EisenhowerMatrixFragment()
            eisenhowerMatrixFragment.setTransitions()
            val tag = EisenhowerMatrixFragment.TAG

            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, eisenhowerMatrixFragment, tag).addToBackStack(tag).commit()
        }

        // configuring the behavior of the bottom sheet (the main layout) that's in front of the backdrop
        // (the small layout containing the button to open the Eisenhower Matrix)
        mainLayout = view.findViewById(R.id.mainLayout)
        val bottomSheetBehavior = BottomSheetBehavior.from(mainLayout)
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isDraggable = true

        rootLayout = view.findViewById(R.id.rootLayout)
        val backdropLayout: ViewGroup = view.findViewById(R.id.backdropLayout)

        // setting the view to be invisible when the sheet is expanded to prevent clicking through the main layout
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                backdropLayout.visibility = when(newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> View.INVISIBLE
                    else -> View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        // setting the height of the collapsed state of the main layout
        view.viewTreeObserver.addOnGlobalLayoutListener {
            bottomSheetBehavior.peekHeight = rootLayout.height - backdropLayout.height + backdropLayout.marginBottom
        }

        expandCollapseIcon = view.findViewById(R.id.expandCollapseIcon)
        expandCollapseIcon.rotation = 180f // the icon has to be flipped to correspond with later animations
        expandCollapseIcon.setOnClickListener {
            // changing the expansion state of the bottom sheet when the icon is clicked
            bottomSheetBehavior.state = when(bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
                else -> BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        headerLayout = view.findViewById(R.id.headerLayout)
        headerLayout.setOnClickListener {
            expandCollapseIcon.callOnClick()
        }

        headerLayoutHandler = HeaderLayoutHandler()

        activityAreaInfoLayout = view.findViewById(R.id.activityAreaInfoLayout)
        activityAreaNameText = view.findViewById(R.id.activityAreaName)
        timeSpentText = view.findViewById(R.id.timeSpentText)
        sentimentText = view.findViewById(R.id.sentimentText)
        successText = view.findViewById(R.id.successText)
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

    override fun onMessageSent(message: Message) {
        if(message is AnswerMessage) {
            val prevActivity = userActivityManager.getPreviousActivity() ?: return

            prevActivity.userRating = message.question.answer
            prevActivity.userRatingType = when(message.question) {
                is TextInputQuestion -> UserActivity.RATING_TYPE_TEXT_INPUT
                is EmojiQuestion -> UserActivity.RATING_TYPE_EMOJI
                is SliderQuestion -> UserActivity.RATING_TYPE_SLIDER
                is MultipleChoiceQuestion -> UserActivity.RATING_TYPE_MULTIPLE_CHOICE
                is ChoosePictureQuestion -> UserActivity.RATING_TYPE_PICTURE
                else -> UserActivity.RATING_TYPE_UNKNOWN
            }

            userActivityManager.save()

            // TODO: return to start screen

            (activity as MainActivity).removeOnMessageSentListener(this)
        }
    }

    override fun onValueSelected(entry: Entry?, h: Highlight?) {
        if(entry == null || entry !is PieEntry)
            return

        if(userActivityManager.getCurrentActivity() == null) {
            activityAreaInfoLayout.visibility = View.GONE
            return
        }

        lateinit var activityArea: UserActivity.Type
        // finding out which value this actually is
        for(i in 0 until chart.data.dataSet.entryCount) {
            val pieEntry = chart.data.dataSet.getEntryForIndex(i)
            if(pieEntry.label == entry.label) {
                // the correct entry was found; returning in case the "time left" segment was selected
                if(pieEntry.label == null) {
                    activityAreaInfoLayout.visibility = View.GONE
                    return
                }

                activityArea = UserActivity.Type.valueOf(pieEntry.label)
                break
            }
        }

        // as soon as a valid selection is found, this info can be visible
        activityAreaInfoLayout.visibility = View.VISIBLE

        val yValueSum = chart.data.yValueSum
        var toAngle = 0f

        val entryIndex = chart.data.dataSet.getEntryIndex(entry)
        for(i in 0 until entryIndex + 1) {
            val pieEntry = chart.data.dataSet.getEntryForIndex(i)
            if(pieEntry.label != null)
                toAngle += (pieEntry.y / yValueSum) * 360f
        }

        chart.spin(250, chart.rotationAngle, 360f - toAngle, Easing.EaseInOutCubic)

        val color = activityArea.getColor(requireContext())

        activityAreaNameText.text = activityArea.getName(requireContext())

        val todaysActivities = userActivityManager.getTodaysActivities()
        val totalMillis = activityArea.getTotalTime(todaysActivities, true)

        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis)

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        timeSpentText.text = getString(R.string.start_time_format, hours, minutes)
        /*sentimentText.text = "Placeholder"
        successText.text = "Placeholder"*/

        // coloring the icons and the headline
        activityAreaNameText.setTextColor(color)

        val states = Array(1) {
            IntArray(1)
        }
        states[0][0] = android.R.attr.state_enabled

        val colors = IntArray(1)
        colors[0] = color

        val colorStateList = ColorStateList(states, colors)

        timeSpentText.compoundDrawablesRelative[0].setTintList(colorStateList)
        sentimentText.compoundDrawablesRelative[0].setTintList(colorStateList)
        successText.compoundDrawablesRelative[0].setTintList(colorStateList)

        // TODO: these views are disabled for now because there's no content that can be used to fill them
        sentimentText.visibility = View.GONE
        successText.visibility = View.GONE
    }

    override fun onNothingSelected() {

    }

}