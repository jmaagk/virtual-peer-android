package me.maagk.johannes.virtualpeer.exercise

import android.content.Context
import androidx.annotation.ColorRes
import me.maagk.johannes.virtualpeer.Utils

abstract class Exercise(private val context: Context, val name: String, val info: String,
                        @ColorRes color: Int, @ColorRes textColor: Int, @ColorRes buttonTextColor: Int,
                        var pinned: Boolean = false) {

    val color = Utils.getColor(context, color)
    val textColor = Utils.getColor(context, textColor)
    val buttonTextColor = Utils.getColor(context, buttonTextColor)

}