package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import me.maagk.johannes.virtualpeer.R

class TextInputQuestionFragment : QuestionFragment(R.layout.fragment_question_text_input) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textInputEditText: TextInputEditText = view.findViewById(R.id.textInput)
        textInputEditText.addTextChangedListener {
            question.answer = textInputEditText.text.toString()
            question.answered = true
        }
    }

}