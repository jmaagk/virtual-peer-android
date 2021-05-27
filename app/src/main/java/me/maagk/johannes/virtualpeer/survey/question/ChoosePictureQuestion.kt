package me.maagk.johannes.virtualpeer.survey.question

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import java.io.Serializable

class ChoosePictureQuestion(question: String, val images: ArrayList<Image>) : Question(question) {

    // this might be confusing: does it start at 0?
    override var answer: Any? = -1

    // the internal representation of one image; this will probably change
    class Image(@DrawableRes var drawableId: Int, var label: String) : Serializable {

        fun getDrawable(context: Context): Drawable? = ResourcesCompat.getDrawable(context.resources, drawableId, context.theme)

    }

}