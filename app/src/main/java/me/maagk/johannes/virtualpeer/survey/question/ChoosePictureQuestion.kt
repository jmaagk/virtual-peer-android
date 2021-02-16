package me.maagk.johannes.virtualpeer.survey.question

import android.graphics.drawable.Drawable

class ChoosePictureQuestion(question: String, val images: ArrayList<Image>) : Question(question) {

    // this might be confusing: does it start at 0?
    override var answer: Any? = -1

    // the internal representation of one image; this will probably change
    class Image(val drawable: Drawable, val label: String)

}