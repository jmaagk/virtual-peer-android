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

    companion object {
        const val TAG = "survey"
    }

    override val actionBarTitle: String
        get() = getString(R.string.nav_drawer_survey)

    private lateinit var survey: Survey
    private lateinit var activity: MainActivity

    private lateinit var surveyFragmentContainer: FragmentContainerView
    private lateinit var startNextButton: Button
    private lateinit var backButton: Button

    private var questionIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = getActivity() as MainActivity

        survey = SurveyStorage(requireContext()).survey
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surveyFragmentContainer = view.findViewById(R.id.surveyFragmentContainer)

        startNextButton = view.findViewById(R.id.startAndNext)
        backButton = view.findViewById(R.id.back)

        startNextButton.setOnClickListener {
            navigate(true)
        }

        backButton.setOnClickListener {
            navigate(false)
        }

        update()
    }

    private fun update() {
        val onInfoScreen = isOnInfoScreen()

        updateFragments()

        when {
            questionIndex == survey.questions.size - 1 -> startNextButton.setText(R.string.survey_finish)
            isOnInfoScreen() -> startNextButton.setText(R.string.survey_start)
            else -> startNextButton.setText(R.string.survey_next)
        }

        backButton.visibility = if(onInfoScreen) View.INVISIBLE else View.VISIBLE
    }

    private fun navigate(forward: Boolean) {
        if(forward) {
            if((questionIndex + 1) < survey.questions.size)
                questionIndex++
        } else {
            questionIndex--
        }

        update()
    }

    private fun updateFragments() {
        var fragment: Fragment? = if(isOnInfoScreen()) {
            val returnValue = SurveyStartFragment()
            returnValue.title = survey.title
            returnValue.description = survey.description
            returnValue
        } else {
            when(val question = survey.questions[questionIndex]) {
                is TextInputQuestion -> TextInputQuestionFragment(question)
                is SliderQuestion -> SliderQuestionFragment(question)
                is EmojiQuestion -> EmojiQuestionFragment(question)
                is MultipleChoiceQuestion -> MultipleChoiceQuestionFragment(question)
                is ChoosePictureQuestion -> ChoosePictureQuestionFragment(question)
                else -> null
            }
        }

        if(fragment == null) {
            TODO("add an error here")
        }

        // TODO: add tags
        activity.supportFragmentManager.beginTransaction().replace(R.id.surveyFragmentContainer, fragment, null).commit()
    }

    private fun isOnInfoScreen() : Boolean {
        return questionIndex == -1
    }

}