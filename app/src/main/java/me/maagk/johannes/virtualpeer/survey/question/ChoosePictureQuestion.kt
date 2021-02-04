package me.maagk.johannes.virtualpeer.survey.question

import android.graphics.drawable.Drawable

class ChoosePictureQuestion(question: String, val images: ArrayList<Image>) : Question(question) {

    // the internal representation of one image; this will probably change
    class Image(val drawable: Drawable)

    // this might be confusing: does it start at 0?
    var chosenPicture: Int = 0

}