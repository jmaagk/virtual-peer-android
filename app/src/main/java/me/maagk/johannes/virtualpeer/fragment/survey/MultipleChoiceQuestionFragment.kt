package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.MultipleChoiceQuestion

class MultipleChoiceQuestionFragment(question: MultipleChoiceQuestion) : QuestionFragment(R.layout.fragment_question_multiple_choice, question) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        if(view != null) {
            val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)

            val multipleChoiceQuestion = question as MultipleChoiceQuestion
            for(choice in multipleChoiceQuestion.choices) {
                val radioButton = inflater.inflate(R.layout.view_radio_button, radioGroup, false) as RadioButton
                radioButton.text = choice
                radioGroup.addView(radioButton)
            }
        }

        return view
    }

}