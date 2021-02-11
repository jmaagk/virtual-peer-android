package me.maagk.johannes.virtualpeer.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.ChoosePictureQuestion

class ChoosePictureQuestionView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    var question: ChoosePictureQuestion? = null
        set(value) {
            field = value
            update()
        }

    val gridLayout: GridLayout

    init {
        inflate(context, R.layout.view_question_choose_picture, this)

        gridLayout = findViewById(R.id.gridLayout)
    }

    fun update() {
        question?.images?.forEach { image ->
            val imageButton = ImageButton(context)
            imageButton.setImageDrawable(image.drawable)
            gridLayout.addView(imageButton)
        }
    }

}