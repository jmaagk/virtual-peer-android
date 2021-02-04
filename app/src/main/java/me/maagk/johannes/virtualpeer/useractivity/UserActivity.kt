package me.maagk.johannes.virtualpeer.useractivity

import android.graphics.Color
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class UserActivity(val type: Type, var startTime: ZonedDateTime, var endTime: ZonedDateTime?) {

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