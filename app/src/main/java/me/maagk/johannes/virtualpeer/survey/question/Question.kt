package me.maagk.johannes.virtualpeer.survey.question

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import me.maagk.johannes.virtualpeer.R

abstract class Question(var question: String) {

    abstract var answer: Any?
    var answered = false

    // TODO: improve this; maybe use automated IDs or better tags?
    var tag: String? = null

    companion object {

        fun getExampleTextInputQuestion(context: Context): TextInputQuestion {
            return TextInputQuestion(context.getString(R.string.lorem_ipsum_medium))
        }

        fun getExampleEmojiQuestion(context: Context): EmojiQuestion {
            val emojis = arrayListOf("\uD83D\uDC4E", "\uD83D\uDC4D")
            return EmojiQuestion(context.getString(R.string.lorem_ipsum_medium), emojis)
        }

        fun getExampleSliderQuestion(context: Context): SliderQuestion {
            return SliderQuestion(context.getString(R.string.lorem_ipsum_medium), 1f, 10f)
        }

        fun getExampleMultipleChoiceQuestion(context: Context): MultipleChoiceQuestion {
            val lorem = context.getString(R.string.lorem_ipsum_short)
            return MultipleChoiceQuestion(lorem, arrayListOf("$lorem 1", "$lorem 2", "$lorem 3"))
        }

        fun getExampleChoosePictureQuestion(context: Context): ChoosePictureQuestion {
            val images = ArrayList<ChoosePictureQuestion.Image>()
            ResourcesCompat.getDrawable(context.resources, R.drawable.test_image_1, context.theme)?.let { images.add(ChoosePictureQuestion.Image(it, "Lorem 1")) }
            ResourcesCompat.getDrawable(context.resources, R.drawable.test_image_2, context.theme)?.let { images.add(ChoosePictureQuestion.Image(it, "Lorem 2")) }
            ResourcesCompat.getDrawable(context.resources, R.drawable.test_image_3, context.theme)?.let { images.add(ChoosePictureQuestion.Image(it, "Lorem 3")) }
            ResourcesCompat.getDrawable(context.resources, R.drawable.test_image_4, context.theme)?.let { images.add(ChoosePictureQuestion.Image(it, "Lorem 4")) }

            return ChoosePictureQuestion(context.getString(R.string.lorem_ipsum_medium), images)
        }

    }

}