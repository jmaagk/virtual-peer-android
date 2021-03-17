package me.maagk.johannes.virtualpeer.fragment

import android.graphics.*
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
import me.maagk.johannes.virtualpeer.MainActivity
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.charting.ActivityPoolChart
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment
import me.maagk.johannes.virtualpeer.survey.question.*
import me.maagk.johannes.virtualpeer.useractivity.UserActivity
import me.maagk.johannes.virtualpeer.useractivity.UserActivityManager
import java.text.DateFormat
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.round

class StartFragment : Fragment(R.layout.fragment_start), FragmentActionBarTitle, ChatFragment.OnMessageSentListener {

    private lateinit var userActivityManager: UserActivityManager

    private lateinit var currentActivityText: TextView
    private lateinit var chart: ActivityPoolChart

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

        chart.update()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentActivityText = view.findViewById(R.id.currentActivityText)
        updateCurrentActivityText()

        val chartDate: TextView = view.findViewById(R.id.chartDate)
        val changeActivityButton: Button = view.findViewById(R.id.currentActivityChange)
        val currentActivityLayout: View = view.findViewById(R.id.currentActivityLayout)
        val changeActivityLayout: View = view.findViewById(R.id.changeActivityLayout)
        val activityRadioGroup: RadioGroup = view.findViewById(R.id.radioGroup)

        chartDate.text = DateFormat.getDateInstance(DateFormat.SHORT).format(Date())

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
                    0 -> ChatFragment.TextInputQuestionMessage(Question.getExampleTextInputQuestion(requireContext()))
                    1 -> ChatFragment.EmojiQuestionMessage(Question.getExampleEmojiQuestion(requireContext()))
                    2 -> ChatFragment.SliderQuestionMessage(Question.getExampleSliderQuestion(requireContext()))
                    3 -> ChatFragment.MultipleChoiceQuestionMessage(Question.getExampleMultipleChoiceQuestion(requireContext()))
                    else -> ChatFragment.ChoosePictureQuestionMessage(Question.getExampleChoosePictureQuestion(requireContext()))
                }
                questionMessage.question.tag = "activity_rating"

                val mainActivity = activity as MainActivity
                mainActivity.queueMessage(questionMessage)
            }

            updateCurrentActivityText()
            group.clearCheck()
        }

        chart = view.findViewById(R.id.startChart)
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

    override fun onMessageSent(message: ChatFragment.Message) {
        if(message is ChatFragment.AnswerMessage) {
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

}