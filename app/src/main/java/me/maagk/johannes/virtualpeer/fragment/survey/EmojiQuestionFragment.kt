package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.View
import android.widget.TextView
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.survey.question.EmojiQuestion

class EmojiQuestionFragment(question: EmojiQuestion) : QuestionFragment(R.layout.fragment_question_emoji, question) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emoji1 = view.findViewById<TextView>(R.id.emoji1)
        val emoji2 = view.findViewById<TextView>(R.id.emoji2)

        val emojiQuestion = question as EmojiQuestion
        emojiQuestion.emojis.forEachIndexed{index, emoji ->
            when(index) {
                0 -> emoji1.text = emoji
                1 -> emoji2.text = emoji
                else -> TODO("do something when more emojis are supplied")
            }
        }
    }

}