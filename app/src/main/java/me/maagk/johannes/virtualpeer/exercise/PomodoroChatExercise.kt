package me.maagk.johannes.virtualpeer.exercise

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.maagk.johannes.virtualpeer.activity.MainActivity
import me.maagk.johannes.virtualpeer.VirtualPeerApp
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.chat.AnswerMessage
import me.maagk.johannes.virtualpeer.chat.Message
import me.maagk.johannes.virtualpeer.chat.MultipleChoiceQuestionMessage
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment
import me.maagk.johannes.virtualpeer.fragment.exercise.AddLearningContentFragment
import me.maagk.johannes.virtualpeer.survey.question.MultipleChoiceQuestion
import java.util.concurrent.TimeUnit

class PomodoroChatExercise(context: Context, chatFragment: ChatFragment) : ChatExercise(context, chatFragment), AddLearningContentFragment.OnLearningContentsFinishedListener {

    companion object {
        fun startLearningContent(context: Context, learningContents: ArrayList<LearningContent>, position: Int) {
            val startTime = System.currentTimeMillis()

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val triggerTimeMillis = startTime + TimeUnit.MINUTES.toMillis(learningContents[position].durationMinutes.toLong())

            val intent = Intent(context, PomodoroAlarmReceiver::class.java)

            // using a Bundle here because there would be really weird bugs otherwise
            val alarmExtras = Bundle()
            alarmExtras.putParcelableArrayList("learningContents", learningContents)
            intent.putExtra("extraBundle", alarmExtras)

            intent.putExtra("position", position)

            val pendingIntent = PendingIntent.getBroadcast(context, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            // setting an exact alarm that will be triggered once the current time is over
            AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)

            // the intent for starting the learning content service; responsible for showing the notification for this content
            val serviceIntent = Intent(context, LearningContentService::class.java)

            // wrapping these extras in a Bundle because not doing so would result in a strange bug
            val serviceExtras = Bundle()
            serviceExtras.putParcelable("learningContent", learningContents[position])
            serviceExtras.putLong("startTime", startTime)

            serviceIntent.putExtras(serviceExtras)
            context.startService(serviceIntent)
        }

        fun finish(context: Context) {
            val notification = FinishNotification(context).build()

            NotificationManagerCompat.from(context).notify(VirtualPeerApp.NOTIFICATION_ID_POMODORO_FINISH, notification)
        }
    }

    private lateinit var learningContentQuestion: MultipleChoiceQuestion

    private lateinit var learningContents: ArrayList<LearningContent>

    private lateinit var learningContentFinishedQuestion: MultipleChoiceQuestion

    private var startTime = 0L

    override fun createStartQuestion(startQuestion: MultipleChoiceQuestion): MultipleChoiceQuestion {
        startQuestion.choices.add(context.getString(R.string.pomodoro_start_message_more_info))
        startQuestion.choices.add(context.getString(R.string.pomodoro_start_message_start))

        startQuestion.question = context.getString(R.string.pomodoro_start_message, userProfile.name)
        return startQuestion
    }

    override fun createMoreInfoQuestion(moreInfoQuestion: MultipleChoiceQuestion): MultipleChoiceQuestion {
        moreInfoQuestion.choices.add(context.getString(R.string.pomodoro_more_info_message_yes))
        moreInfoQuestion.choices.add(context.getString(R.string.pomodoro_more_info_message_not_now))

        moreInfoQuestion.question = context.getString(R.string.pomodoro_more_info_message)
        return moreInfoQuestion
    }

    override fun createRateQuestion(ratingQuestion: MultipleChoiceQuestion): MultipleChoiceQuestion {
        ratingQuestion.choices.add(context.getString(R.string.pomodoro_finish_message_got_everything_done))
        ratingQuestion.choices.add(context.getString(R.string.pomodoro_finish_message_didnt_get_everything_done))

        ratingQuestion.question = context.getString(R.string.pomodoro_finish_message)
        return ratingQuestion
    }

    override fun start() {
        val options = arrayListOf<String>()
        options.add(context.getString(R.string.pomodoro_learning_content_message_ready))
        options.add(context.getString(R.string.pomodoro_learning_content_message_back))

        val learningContentMessageString = context.getString(R.string.pomodoro_learning_content_message)

        learningContentQuestion = MultipleChoiceQuestion(learningContentMessageString, options)
        val learningContentMessage = MultipleChoiceQuestionMessage(learningContentQuestion)

        sendMessage(learningContentMessage)
    }

    override fun onMessageSent(message: Message) {
        super.onMessageSent(message)

        if(message is AnswerMessage) {
            val answeredQuestion = message.answeredQuestion

            if(::learningContentQuestion.isInitialized && answeredQuestion == learningContentQuestion && learningContentQuestion.answered) {
                when(learningContentQuestion.answer as Int) {
                    0 -> {
                        val learningContentFragment = AddLearningContentFragment()
                        learningContentFragment.onLearningContentsFinishedListener = this

                        val tag = AddLearningContentFragment.TAG
                        chatFragment.parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, learningContentFragment, tag).addToBackStack(tag).commit()
                    }

                    1 -> {
                        // TODO: go back
                    }
                }
            }

            if(::learningContentFinishedQuestion.isInitialized && answeredQuestion == learningContentFinishedQuestion && learningContentFinishedQuestion.answered) {
                when(learningContentFinishedQuestion.answer as Int) {
                    0 -> {
                        // this is the point at which the exercise *really* starts
                        startTime = System.currentTimeMillis()

                        startLearningContent(context, learningContents, 0)
                    }

                    1 -> {
                        // TODO: go back
                    }
                }
            }
        }
    }

    override fun onRateQuestionAnswered() {
        when(rateQuestion.answer as Int) {
            0 -> {
                val doneMessage = Message(Message.INCOMING, context.getString(R.string.pomodoro_got_everything_done_message))
                sendMessage(doneMessage)
            }

            1 -> {
                val followUpOptions = arrayListOf<String>()
                followUpOptions.add(context.getString(R.string.pomodoro_didnt_get_everything_done_message_option_1))
                followUpOptions.add(context.getString(R.string.pomodoro_didnt_get_everything_done_message_option_2))
                followUpOptions.add(context.getString(R.string.pomodoro_didnt_get_everything_done_message_option_3))

                val followUpQuestion = MultipleChoiceQuestion("", followUpOptions)
                val followUpMessage = MultipleChoiceQuestionMessage(followUpQuestion)

                sendMessage(followUpMessage)
            }
        }
    }

    override fun onLearningContentsFinished(learningContents: ArrayList<LearningContent>) {
        this.learningContents = learningContents

        val options = arrayListOf(
                context.getString(R.string.pomodoro_learning_content_finished_message_ready),
                context.getString(R.string.pomodoro_learning_content_finished_message_back))

        learningContentFinishedQuestion = MultipleChoiceQuestion(context.getString(R.string.pomodoro_learning_content_finished_message), options)
        val message = MultipleChoiceQuestionMessage(learningContentFinishedQuestion)

        // TODO: fix scrolling with this message
        sendMessage(message)
    }

    class FinishNotification(val context: Context) {

        fun build(): android.app.Notification {
            val chatIntent = Intent(context, MainActivity::class.java)
            chatIntent.putExtra("rateExercise", PomodoroChatExercise::class.java)
            chatIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val contentIntent = PendingIntent.getActivity(context, -1, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            return NotificationCompat.Builder(context, VirtualPeerApp.CHANNEL_POMODORO)
                    .setSmallIcon(R.drawable.ic_survey)
                    .setContentTitle(context.getString(R.string.pomodoro_notification_finish_title))
                    .setContentText(context.getString(R.string.pomodoro_notification_finish_content))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setColor(Utils.getColor(context, R.color.colorPrimary))
                    .setContentIntent(contentIntent)
                   .build()
        }

    }

    class Notification(val context: Context, val content: LearningContent, private val timeLeftMillis: Long) {

        fun build(): android.app.Notification {
            val totalSeconds = timeLeftMillis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60

            val text = if(content is LearningContent.Break) {
                if(minutes > 0)
                    context.getString(R.string.pomodoro_notification_content_break, minutes, seconds)
                else
                    context.getString(R.string.pomodoro_notification_content_break_seconds, seconds)
            } else {
                if(minutes > 0)
                    context.getString(R.string.pomodoro_notification_content_learning_content, content.content, minutes, seconds)
                else
                    context.getString(R.string.pomodoro_notification_content_learning_content_seconds, content.content, seconds)
            }

            return NotificationCompat.Builder(context, VirtualPeerApp.CHANNEL_POMODORO)
                    .setSmallIcon(R.drawable.ic_survey) // TODO: change this icon
                    .setContentTitle(context.getString(R.string.pomodoro_notification_title))
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setOnlyAlertOnce(true)
                    .setColor(Utils.getColor(context, R.color.colorPrimary))
                    .build()
        }

    }

}