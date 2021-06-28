package me.maagk.johannes.virtualpeer.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.forEachIndexed
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.MultipleChoiceQuestion

class MultipleChoiceQuestionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), DefaultListenerController {

    override var setDefaultListener: Boolean = true

    var question: MultipleChoiceQuestion? = null
        set(value) {
            field = value
            update()
        }

    val radioGroup: RadioGroup

    init {
        inflate(context, R.layout.view_question_multiple_choice, this)

        radioGroup = findViewById(R.id.radioGroup)
    }

    fun update() {
        radioGroup.removeAllViews()

        val inflater = LayoutInflater.from(context)

        // TODO: improve this process by not inflating every time this binds to a message
        question?.choices?.forEach {
            val radioButton = inflater.inflate(R.layout.view_radio_button, radioGroup, false) as RadioButton
            radioButton.text = it
            radioGroup.addView(radioButton)
        }

        if(setDefaultListener) {
            radioGroup.setOnCheckedChangeListener { radioGroup, id ->
                radioGroup.forEachIndexed { index, view ->
                    if(view.id == id) {
                        question?.answer = index
                        question?.answered = true
                        return@setOnCheckedChangeListener
                    }
                }
            }
        }
    }

}