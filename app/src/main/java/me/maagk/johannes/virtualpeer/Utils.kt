package me.maagk.johannes.virtualpeer

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.text.format.DateFormat
import android.util.DisplayMetrics
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.transition.AutoTransition
import okhttp3.HttpUrl
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.round

class Utils {

    companion object {

        fun newServerUrlBuilder(): HttpUrl.Builder = HttpUrl.Builder()
            .scheme("https")
            .host("vetwe16vp.vetmed.fu-berlin.de")
            .port(443)

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

        fun dpToPx(dp: Float, displayMetrics: DisplayMetrics): Float {
            return dp * displayMetrics.density + 0.5f
        }

        fun pxToDp(px: Int, displayMetrics: DisplayMetrics): Int {
            return ceil((px - 0.5) / displayMetrics.density).toInt()
        }

        fun getAnimatorDurationScale(context: Context): Float {
            return Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
        }

        fun getScaledAnimationDuration(context: Context, duration: Long): Long {
            return (duration * getAnimatorDurationScale(context)).toLong()
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

        fun SharedPreferences.containsNonNullAndNonBlankValue(key: String): Boolean {
            return contains(key) && !getString(key, null).isNullOrBlank()
        }

        fun newFadeAnimation(fadeIn: Boolean, duration: Long): AlphaAnimation {
            val from = if(fadeIn) 0f else 1f
            val to = if(fadeIn) 1f else 0f

            val fadeAnimation = AlphaAnimation(from, to)
            fadeAnimation.interpolator = LinearInterpolator()
            fadeAnimation.duration = duration
            return fadeAnimation
        }

        fun LocalDate.getFormattedDate(context: Context): String {
            val dateFormat = DateFormat.getDateFormat(context)
            val oldDate = Date.from(this.atStartOfDay(ZoneId.systemDefault())?.toInstant())
            return dateFormat.format(oldDate)
        }

    }

}