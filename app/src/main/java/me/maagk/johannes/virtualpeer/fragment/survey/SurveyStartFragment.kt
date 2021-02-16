package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import me.maagk.johannes.virtualpeer.R

class SurveyStartFragment : Fragment(R.layout.fragment_survey_start) {

    lateinit var title: String
    lateinit var description: String

    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleText = view.findViewById(R.id.title)
        descriptionText = view.findViewById(R.id.description)

        titleText.text = title
        descriptionText.text = description
    }

}