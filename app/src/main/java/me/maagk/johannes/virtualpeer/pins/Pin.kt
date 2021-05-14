package me.maagk.johannes.virtualpeer.pins

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import kotlin.math.abs

abstract class Pin @JvmOverloads constructor(var size: Size, @ColorInt var color: Int = -1) {

    class EmptyPin : Pin(Size.SMALL)

    enum class Size {
        // 1,  2,      6 columns
        SMALL, NORMAL, LARGE
    }

    fun getColorOnBackground(context: Context): Int {
        val isBackgroundDark = ColorUtils.calculateLuminance(color) < 0.5
        val colorResourceInt = if(isBackgroundDark) R.color.colorTextDark else R.color.colorTextLight
        return Utils.getColor(context, colorResourceInt)
    }

    /**
     * Calculates whether a tint is needed for text colors and icon tints
     */
    fun isTintNeeded(context: Context): Boolean {
        val luminanceBackground = ColorUtils.calculateLuminance(color)
        val luminanceCurrentTint = ColorUtils.calculateLuminance(Utils.getColor(context, R.color.colorText))

        // TODO: this is a bit arbitrary, what's the proper way to do this?
        return abs(luminanceBackground - luminanceCurrentTint) < 0.3
    }

}