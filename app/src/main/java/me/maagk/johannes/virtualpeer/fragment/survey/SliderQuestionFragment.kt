package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.View
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.SliderQuestion
import me.maagk.johannes.virtualpeer.view.SliderQuestionView

class SliderQuestionFragment : QuestionFragment(R.layout.fragment_question_slider) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sliderQuestionView: SliderQuestionView = view.findViewById(R.id.sliderQuestionView)
        sliderQuestionView.question = question as SliderQuestion
    }

}