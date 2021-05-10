package me.maagk.johannes.virtualpeer.exercise

import android.content.Context
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment

class MeditationExercise(context: Context) : Exercise(context, context.getString(R.string.meditation_name), context.getString(R.string.meditation_info),
        R.color.colorExerciseMeditation, R.color.colorTextLight, R.color.colorTextLight) {

    override fun createChatExercise(chatFragment: ChatFragment): ChatExercise = MeditationChatExercise(context, chatFragment)

    override fun getIconResourceId(): Int = R.drawable.ic_meditation

}