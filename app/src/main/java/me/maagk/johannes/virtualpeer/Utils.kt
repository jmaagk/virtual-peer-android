package me.maagk.johannes.virtualpeer

import android.content.Context
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.transition.AutoTransition
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.round

class Utils {

    companion object {

        fun log(message: String) {
            Log.d("Virtual Peer", message)
        }

        fun getColor(context: Context, @ColorRes colorId: Int): Int {
            return ResourcesCompat.getColor(context.resources, colorId, context.theme)
        }

        fun round(value: Float, precision: Int): Double {
            val scale = 10.0.pow(precision)
            return round(value * scale) / scale
        }

        fun dpToPx(dp: Float, displayMetrics: DisplayMetrics): Int {
            return (dp * displayMetrics.density + 0.5f).toInt()
        }

        fun pxToDp(px: Int, displayMetrics: DisplayMetrics): Int {
            return ceil((px - 0.5) / displayMetrics.density).toInt()
        }

        fun getAnimatorDurationScale(context: Context): Float {
            return Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
        }

        // TODO: add transitions to fragments
        fun Fragment.setTransitions() {
            reenterTransition = AutoTransition()
            returnTransition = AutoTransition()
            enterTransition = AutoTransition()
            exitTransition = AutoTransition()

            allowEnterTransitionOverlap = true
            allowReturnTransitionOverlap = true
        }

    }

}