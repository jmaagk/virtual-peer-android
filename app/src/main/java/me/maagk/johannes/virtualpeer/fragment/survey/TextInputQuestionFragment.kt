package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.View
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.TextInputQuestion

class TextInputQuestionFragment(question: TextInputQuestion) : QuestionFragment(R.layout.fragment_question_text_input, question) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}