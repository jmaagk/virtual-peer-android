package me.maagk.johannes.virtualpeer.exercise

import android.content.Context
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment

class BoxBreathingExercise(context: Context) : Exercise(context, context.getString(R.string.box_breathing_name), context.getString(R.string.box_breathing_info),
        R.color.colorExerciseBoxBreathing, R.color.colorTextContrast, R.color.colorExerciseBoxBreathing) {

    override val internalName: String
        get() = "boxBreathing"

    override fun createChatExercise(chatFragment: ChatFragment): ChatExercise = BoxBreathingChatExercise(context, this, chatFragment)

    override fun getIconResourceId(): Int = R.drawable.ic_box_breathing

}