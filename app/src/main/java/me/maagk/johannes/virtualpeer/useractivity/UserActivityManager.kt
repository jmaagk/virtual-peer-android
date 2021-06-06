package me.maagk.johannes.virtualpeer.useractivity

import android.content.Context
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

class UserActivityManager(private val context: Context, private val storage: UserActivityStorage = UserActivityStorage(context)) {

    val timeZone = storage.timeZone

    fun addActivity(activity: UserActivity) {
        storage.userActivities.add(activity)
        save()
    }

    fun setCurrentActivity(activity: UserActivity) {
        // ending the previous activity if it hasn't already ended
        // the current activity's end time will be the new activity's start time
        val current = getCurrentActivity()
        if(current != null && current.endTime == null)
            current.endTime = activity.startTime

        addActivity(activity)
    }

    fun getCurrentActivity(): UserActivity? {
        return storage.getNewestActivity()
    }

    // TODO: does this always do the right thing? (compare it to storage.getNewestActivity())
    fun getPreviousActivity(): UserActivity? {
        val todaysActivities = storage.userActivities
        if(todaysActivities.size >= 2)
            return todaysActivities[todaysActivities.size - 2]

        return null
    }

    fun getTodaysActivities(): List<UserActivity> {
        val startOfToday = LocalDate.now().atStartOfDay(timeZone)
        return getActivitiesStartingAtTime(startOfToday)
    }

    fun getActivitiesStartingAtTime(time: Long, correctStartTime: Boolean = true, cutEndTime: Boolean = false, includeCurrent: Boolean = true): List<UserActivity> {
        val instant = if(time == -1L) Instant.EPOCH else Instant.ofEpochMilli(time)
        val start = ZonedDateTime.ofInstant(instant, timeZone)
        return getActivitiesStartingAtTime(start, correctStartTime, cutEndTime, includeCurrent)
    }

    fun getActivitiesStartingAtTime(start: ZonedDateTime,
        correctStartTime: Boolean = true, cutEndTime: Boolean = false, includeCurrent: Boolean = true): List<UserActivity> {

        val activities = mutableListOf<UserActivity>()

        for(activity in storage.userActivities) {
            var startTime = activity.startTime
            var endTime = activity.endTime

            // skipping the current activity if set to do so
            if(!includeCurrent && endTime == null)
                continue

            // only including activities that either started or ended today
            if(startTime.isBefore(start) && (endTime != null && endTime.isBefore(start)))
                continue

            // correcting the start time so it's only within the span of today
            if(correctStartTime && startTime.isBefore(start))
                startTime = start

            /*
             * Cutting off the end here;
             * this can only happen to the newest activity (the one that hasn't been finished yet).
             *
             * The time this gets executed at will then be the end time of this activity.
             * This will NOT be made persistent and is only forwarded to the caller of this method
             */
            if(cutEndTime && endTime == null)
                endTime = ZonedDateTime.now(timeZone)

            // creating a copy instead of passing the original object to keep the original time values
            activities.add(activity.copy(startTime = startTime, endTime = endTime))
        }

        return activities
    }

    fun save() {
        // TODO: this should probably be done asynchronously
        storage.save()
    }

}