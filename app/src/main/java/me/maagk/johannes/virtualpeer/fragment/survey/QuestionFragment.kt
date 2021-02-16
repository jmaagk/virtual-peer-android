package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.Question

abstract class QuestionFragment(@LayoutRes layoutId: Int, var question: Question) : Fragment(layoutId) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val questionText = view.findViewById<TextView>(R.id.question)
        questionText.text = question.question
    }

}