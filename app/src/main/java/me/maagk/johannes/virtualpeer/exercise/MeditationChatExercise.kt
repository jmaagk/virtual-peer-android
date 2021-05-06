package me.maagk.johannes.virtualpeer.exercise

import android.content.Context
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment
import me.maagk.johannes.virtualpeer.fragment.exercise.MeditationFragment
import me.maagk.johannes.virtualpeer.survey.question.MultipleChoiceQuestion

class MeditationChatExercise(context: Context, chatFragment: ChatFragment) : ChatExercise(context, chatFragment) {

    override fun start() {
        val meditationFragment = MeditationFragment()

        val tag = MeditationFragment.TAG
        chatFragment.parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, meditationFragment, tag).addToBackStack(tag).commit()
    }

    override fun createStartQuestion(startQuestion: MultipleChoiceQuestion): MultipleChoiceQuestion {
        startQuestion.choices.add(context.getString(R.string.meditation_start_message_option_1))
        startQuestion.choices.add(context.getString(R.string.meditation_start_message_option_2))

        startQuestion.question = context.getString(R.string.meditation_start_message, userProfile.name)
        return startQuestion
    }

    override fun createMoreInfoQuestion(moreInfoQuestion: MultipleChoiceQuestion): MultipleChoiceQuestion {
        moreInfoQuestion.choices.add(context.getString(R.string.meditation_more_info_message_option_1))
        moreInfoQuestion.choices.add(context.getString(R.string.meditation_more_info_message_option_2))

        moreInfoQuestion.question = context.getString(R.string.meditation_more_info_message)
        return moreInfoQuestion
    }

    override fun createRateQuestion(ratingQuestion: MultipleChoiceQuestion): MultipleChoiceQuestion {
        TODO("Not yet implemented")
    }

    override fun onRateQuestionAnswered() {
        TODO("Not yet implemented")
    }

}