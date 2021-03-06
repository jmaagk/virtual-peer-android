package me.maagk.johannes.virtualpeer.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.android.material.slider.Slider
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.SliderQuestion

class SliderQuestionView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), DefaultListenerController {

    override var setDefaultListener: Boolean = true

    var question: SliderQuestion? = null
        set(value) {
            field = value
            update()
        }

    val slider: Slider

    init {
        orientation = VERTICAL

        inflate(context, R.layout.view_question_slider, this)

        slider = findViewById(R.id.slider)
    }

    fun update() {
        if(setDefaultListener) {
            slider.addOnChangeListener { _, value, fromUser ->
                if(fromUser) {
                    question?.let {
                        it.answer = value.coerceAtLeast(it.min)
                        it.answered = true
                    }
                }
            }
        }

        question?.let {
            slider.valueFrom = it.min
            slider.valueTo = it.max

            slider.value = (it.answer as Float).coerceAtLeast(it.min)
        }
    }

}