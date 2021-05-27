package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.Question

abstract class QuestionFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    lateinit var question: Question

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val questionSerializable = requireArguments().getSerializable("question")

        require(questionSerializable is Question)
        question = questionSerializable
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val questionText = view.findViewById<TextView>(R.id.question)
        questionText.text = question.question
    }

}