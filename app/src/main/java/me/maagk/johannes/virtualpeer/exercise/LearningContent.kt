package me.maagk.johannes.virtualpeer.exercise

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class LearningContent(val content: String, val durationMinutes: Int) : Parcelable {

    /*
     * this might seem unnecessary, BUT:
     * breaks would be parcelized as regular instances of LearningContent and could thus not be
     * identified later as they'd all turned into regular instances
     */
    @Parcelize
    class Break(private val breakDuration: Int) : LearningContent("", breakDuration), Parcelable

}