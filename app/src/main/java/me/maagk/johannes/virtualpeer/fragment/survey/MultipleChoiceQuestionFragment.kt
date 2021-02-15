package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.MultipleChoiceQuestion
import me.maagk.johannes.virtualpeer.view.MultipleChoiceQuestionView

class MultipleChoiceQuestionFragment(question: MultipleChoiceQuestion) : QuestionFragment(R.layout.fragment_question_multiple_choice, question) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        if(view != null) {
            val multipleChoiceQuestionView: MultipleChoiceQuestionView = view.findViewById(R.id.multipleChoiceQuestionView)
            multipleChoiceQuestionView.question = question as MultipleChoiceQuestion
        }

        return view
    }

}