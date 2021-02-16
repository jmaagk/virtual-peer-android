package me.maagk.johannes.virtualpeer.useractivity

import android.graphics.Color
import java.time.LocalDateTime
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

    enum class Type(val color: Int) {
        POOL_WORK(Color.parseColor("#2196F3")),
        POOL_ESSENTIAL(Color.parseColor("#64B5F6")),
        POOL_REWARDS(Color.parseColor("#1976D2"))
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