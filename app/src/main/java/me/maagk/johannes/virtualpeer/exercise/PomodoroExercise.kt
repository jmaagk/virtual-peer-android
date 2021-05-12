package me.maagk.johannes.virtualpeer.exercise

import android.content.Context
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment

class PomodoroExercise(context: Context) : Exercise(context, context.getString(R.string.pomodoro_name), context.getString(R.string.pomodoro_info),
        R.color.colorExercisePomodoro, R.color.colorTextContrast, R.color.colorExercisePomodoro) {

    override val internalName: String
        get() = "pomodoro"

    override fun createChatExercise(chatFragment: ChatFragment): ChatExercise = PomodoroChatExercise(context, this, chatFragment)

    override fun getIconResourceId(): Int = R.drawable.ic_pomodoro

}