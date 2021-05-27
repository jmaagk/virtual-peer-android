package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import me.maagk.johannes.virtualpeer.activity.MainActivity
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

    private lateinit var surveyStorage: SurveyStorage

    private var questionIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = getActivity() as MainActivity

        val surveySerializable = requireArguments().getSerializable("survey")

        require(surveySerializable is Survey)
        survey = surveySerializable

        surveyStorage = SurveyStorage(requireContext())

        savedInstanceState?.let {
            if(it.containsKey("questionIndex"))
                questionIndex = it.getInt("questionIndex")
        }
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

        surveyStorage.save(survey)

        update()
    }

    private fun updateFragments() {
        var fragment = if(isOnInfoScreen()) {
            SurveyStartFragment().also {
                val bundle = Bundle()
                bundle.putString("title", survey.title)
                bundle.putString("description", survey.description)
                it.arguments = bundle
            }
        } else {
            when(survey.questions[questionIndex]) {
                is TextInputQuestion -> TextInputQuestionFragment()
                is SliderQuestion -> SliderQuestionFragment()
                is EmojiQuestion -> EmojiQuestionFragment()
                is MultipleChoiceQuestion -> MultipleChoiceQuestionFragment()
                is ChoosePictureQuestion -> ChoosePictureQuestionFragment()
                else -> null
            }.also {
                val questionBundle = Bundle()
                questionBundle.putSerializable("question", survey.questions[questionIndex])
                it?.arguments = questionBundle
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

    override fun onPause() {
        super.onPause()

        surveyStorage.save(survey)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("questionIndex", questionIndex)
    }

}