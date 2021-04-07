package me.maagk.johannes.virtualpeer.useractivity

import android.content.Context
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class UserActivity(val type: Type, var startTime: ZonedDateTime, var endTime: ZonedDateTime?) {

    // TODO: there should be a better solution than declaring these types here
    companion object {
        const val RATING_TYPE_UNKNOWN = -1
        const val RATING_TYPE_TEXT_INPUT = 0
        const val RATING_TYPE_EMOJI = 1
        const val RATING_TYPE_SLIDER = 2
        const val RATING_TYPE_MULTIPLE_CHOICE = 3
        const val RATING_TYPE_PICTURE = 4
    }

    var userRatingType: Int = RATING_TYPE_UNKNOWN
    var userRating: Any? = null

    enum class Type {
        POOL_WORK, POOL_ESSENTIAL, POOL_REWARDS;

        fun getColor(context: Context): Int {
            return when(this) {
                POOL_WORK -> Utils.getColor(context, R.color.colorActivityWork)
                POOL_ESSENTIAL -> Utils.getColor(context, R.color.colorActivityEssential)
                POOL_REWARDS -> Utils.getColor(context, R.color.colorActivityRewards)
            }
        }

        fun getName(context: Context): String {
            return when(this) {
                POOL_WORK -> context.getString(R.string.user_activity_type_work)
                POOL_ESSENTIAL -> context.getString(R.string.user_activity_type_essential)
                POOL_REWARDS -> context.getString(R.string.user_activity_type_rewards)
            }
        }
    }

    fun getDuration(): Long {
        return getDuration(false)
    }

    fun getDuration(toNow: Boolean): Long {
        return if(endTime == null) {
            if(toNow)
                startTime.until(ZonedDateTime.now(), ChronoUnit.MILLIS)
            else
                -1
        } else {
            startTime.until(endTime, ChronoUnit.MILLIS)
        }
    }

}