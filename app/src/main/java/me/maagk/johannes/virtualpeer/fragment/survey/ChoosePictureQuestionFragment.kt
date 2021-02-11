package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.ChoosePictureQuestion

class ChoosePictureQuestionFragment(question: ChoosePictureQuestion) : QuestionFragment(R.layout.fragment_question_choose_picture, question) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val choosePictureQuestion = question as ChoosePictureQuestion
        val gridLayout: GridLayout = view.findViewById(R.id.gridLayout)

        for(image in choosePictureQuestion.images) {
            val imageButton = ImageButton(requireContext())
            imageButton.setImageDrawable(image.drawable)
            gridLayout.addView(imageButton)
        }
    }

}