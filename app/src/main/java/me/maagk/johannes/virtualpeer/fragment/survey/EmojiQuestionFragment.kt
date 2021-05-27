package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.View
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.EmojiQuestion
import me.maagk.johannes.virtualpeer.view.EmojiQuestionView

class EmojiQuestionFragment : QuestionFragment(R.layout.fragment_question_emoji) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emojiQuestionView: EmojiQuestionView = view.findViewById(R.id.emojiQuestionView)
        emojiQuestionView.question = question as EmojiQuestion
    }

}