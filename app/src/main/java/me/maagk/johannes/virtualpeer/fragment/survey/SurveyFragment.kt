package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import me.maagk.johannes.virtualpeer.MainActivity
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.fragment.FragmentActionBarTitle
import me.maagk.johannes.virtualpeer.survey.Survey
import me.maagk.johannes.virtualpeer.survey.SurveyStorage
import me.maagk.johannes.virtualpeer.survey.question.*

class SurveyFragment : Fragment(R.layout.fragment_survey), FragmentActionBarTitle {

    override val actionBarTitle: String
        get() = getString(R.string.nav_drawer_survey)

    private lateinit var survey: Survey
    private lateinit var activity: MainActivity

    private lateinit var surveyInfoLayout: LinearLayout
    private lateinit var surveyFragmentContainer: FragmentContainerView
    private lateinit var startNextButton: Button
    private lateinit var backButton: Button

    private var questionIndex: Int = -1
    private var onInfoScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = getActivity() as MainActivity

        survey = SurveyStorage(requireContext()).survey
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surveyInfoLayout = view.findViewById(R.id.surveyInfoLayout)
        surveyFragmentContainer = view.findViewById(R.id.surveyFragmentContainer)

        val titleText: TextView = surveyInfoLayout.findViewById(R.id.title)
        val descriptionText: TextView = surveyInfoLayout.findViewById(R.id.description)

        titleText.text = survey.title
        descriptionText.text = survey.description

        startNextButton = view.findViewById(R.id.startAndNext)
        backButton = view.findViewById(R.id.back)

        startNextButton.setOnClickListener {
            navigate(true)
        }

        backButton.setOnClickListener {
            navigate(false)
        }
    }

    private fun navigate(forward: Boolean) {
        if(forward) {
            if(onInfoScreen)
                updateInfoScreen(false)

            if((questionIndex + 1) < survey.questions.size) {
                questionIndex++
                updateQuestion()
            }
        } else {
            questionIndex--

            if(questionIndex == -1)
                updateInfoScreen(true)
            else
                updateQuestion()
        }

        if(questionIndex == survey.questions.size - 1)
            startNextButton.setText(R.string.survey_finish)
        else if(!onInfoScreen)
            startNextButton.setText(R.string.survey_next)
    }

    private fun updateQuestion() {
        val question = survey.questions[questionIndex]
        var fragment: Fragment?

        fragment = when(question) {
            is TextInputQuestion -> TextInputQuestionFragment(question)
            is SliderQuestion -> SliderQuestionFragment(question)
            is EmojiQuestion -> EmojiQuestionFragment(question)
            is MultipleChoiceQuestion -> MultipleChoiceQuestionFragment(question)
            is ChoosePictureQuestion -> ChoosePictureQuestionFragment(question)
            else -> null
        }

        if(fragment == null) {
            TODO("add an error here")
        }

        // TODO: add tags
        activity.supportFragmentManager.beginTransaction().replace(R.id.surveyFragmentContainer, fragment, null).commit()
    }

    private fun updateInfoScreen(show: Boolean) {
        surveyInfoLayout.visibility = if(show) View.VISIBLE else View.GONE
        surveyFragmentContainer.visibility = if(show) View.GONE else View.VISIBLE
        startNextButton.setText(if(show) R.string.survey_start else R.string.survey_next)
        backButton.visibility = if(show) View.INVISIBLE else View.VISIBLE
        questionIndex = if(show) -1 else 0

        onInfoScreen = show
    }

}