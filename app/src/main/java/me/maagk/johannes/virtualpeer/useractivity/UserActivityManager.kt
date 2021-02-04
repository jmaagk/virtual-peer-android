package me.maagk.johannes.virtualpeer.useractivity

import android.content.Context
import java.time.ZoneId
import java.time.ZonedDateTime

class UserActivityManager(private val context: Context) {

    private val storage = UserActivityStorage(context)

    init {

    }

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

    fun getTodaysActivities(): ArrayList<UserActivity> {
        val activities = arrayListOf<UserActivity>()
        val startOfToday = ZonedDateTime.now().toLocalDate().atStartOfDay()

        for(activity in storage.userActivities) {
            var startTime = activity.startTime
            val endTime = activity.endTime

            // only including activities that either started or ended today
            if(startTime.toLocalDateTime().isBefore(startOfToday) && (endTime != null && endTime.toLocalDateTime().isBefore(startOfToday)))
                continue

            // correcting the start time so it's only within the span of today
            if(startTime.toLocalDateTime().isBefore(startOfToday))
                startTime = startOfToday.atZone(ZoneId.systemDefault())

            // creating a copy instead of passing the original object to keep the original time values
            activities.add(activity.copy(startTime = startTime))
        }

        return activities
    }

    fun save() {
        // TODO: this should probably be done asynchronously
        storage.save()
    }

}