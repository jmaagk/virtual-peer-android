package me.maagk.johannes.virtualpeer.survey.question

import android.content.Context
import me.maagk.johannes.virtualpeer.R
import java.io.Serializable

abstract class Question(var question: String) : Serializable {

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
            images.add(ChoosePictureQuestion.Image(R.drawable.test_image_1, "Lorem 1"))
            images.add(ChoosePictureQuestion.Image(R.drawable.test_image_2, "Lorem 2"))
            images.add(ChoosePictureQuestion.Image(R.drawable.test_image_3, "Lorem 3"))
            images.add(ChoosePictureQuestion.Image(R.drawable.test_image_4, "Lorem 4"))

            return ChoosePictureQuestion(context.getString(R.string.lorem_ipsum_medium), images)
        }

    }

}