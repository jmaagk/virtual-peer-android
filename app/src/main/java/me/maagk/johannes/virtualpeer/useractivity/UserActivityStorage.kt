package me.maagk.johannes.virtualpeer.useractivity

import android.content.Context
import me.maagk.johannes.virtualpeer.Storage
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class UserActivityStorage(context: Context, refresh: Boolean = true) : Storage<UserActivity>(context, refresh) {

    override val FILE_NAME: String
        get() = "activities.xml"
    override val VERSION: Int
        get() = 1

    lateinit var timeZone: ZoneId

    // just an alias to have the name make a bit more sense
    val userActivities = items

    init {
        // making sure a time zone is set
        // this should only run when the app is started for the first time
        if(!::timeZone.isInitialized)
            timeZone = ZoneId.systemDefault()
    }

    override fun refreshList(doc: Document) {
        val activitiesTag = doc.documentElement
        if(activitiesTag.tagName != "activities") {
            // TODO: some error handling might be needed
        }

        val fileVersion = activitiesTag.getAttribute("version").toInt()
        timeZone = ZoneId.of(activitiesTag.getAttribute("timeZone"))

        val activityTags = activitiesTag.getElementsByTagName("activity")
        for(i in 0 until activityTags.length) {
            val activityTag = activityTags.item(i)
            val activity = parseItem(activityTag, fileVersion)
            items.add(activity)
        }
    }

    override fun parseItem(tag: Node, version: Int): UserActivity {
        val type = UserActivity.Type.valueOf(tag.attributes.getNamedItem("type").nodeValue.toUpperCase(Locale.ROOT))

        val startTime = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(
                    tag.attributes.getNamedItem("startTime").nodeValue.toLong()),
                        timeZone)

        val endTimeLong = tag.attributes.getNamedItem("endTime").nodeValue.toLong()
        val endTime = if(endTimeLong == -1L) {
            null
        } else {
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(endTimeLong), timeZone)
        }

        val activity = UserActivity(type, startTime, endTime)

        val userRatingTypeTag = tag.attributes.getNamedItem("userRatingType")
        val userRatingTag = tag.attributes.getNamedItem("userRating")
        if(userRatingTypeTag != null && userRatingTag != null) {
            activity.userRatingType = userRatingTypeTag.nodeValue.toInt()
            activity.userRating = when(activity.userRatingType) {
                UserActivity.RATING_TYPE_EMOJI,
                UserActivity.RATING_TYPE_MULTIPLE_CHOICE,
                UserActivity.RATING_TYPE_PICTURE -> userRatingTag.nodeValue.toInt()

                UserActivity.RATING_TYPE_SLIDER -> userRatingTag.nodeValue.toFloat()

                else -> userRatingTag.nodeValue
            }
        }

        return activity
    }

    override fun getRootElement(doc: Document): Element {
        val root = doc.createElement("activities")
        root.setAttribute("timeZone", ZoneId.systemDefault().id)

        return root
    }

    override fun convertItemToXml(item: UserActivity, doc: Document): Element {
        val activity = item

        val activityRoot = doc.createElement("activity")

        activityRoot.setAttribute("type", activity.type.toString())
        activityRoot.setAttribute("startTime", activity.startTime.toInstant().toEpochMilli().toString())

        val endTimeMillis = if(activity.endTime == null) -1 else activity.endTime!!.toInstant().toEpochMilli()
        activityRoot.setAttribute("endTime", endTimeMillis.toString())

        if(activity.userRating != null) {
            activityRoot.setAttribute("userRatingType", activity.userRatingType.toString())
            activityRoot.setAttribute("userRating", activity.userRating.toString())
        }

        return activityRoot
    }

    override fun updateInternal(fromVersion: Int): Int {
        // just a quick move to a new variable as arguments can't be reassigned in Kotlin
        var updatedVersion = fromVersion

        /*
         * some code to update the version we're now on
         * switch without break would usually be used here to "fall" through necessary
         * updates consecutively but it doesn't exist in Kotlin and when doesn't do the same thing
         */
        if(updatedVersion == 1) {
            // future update code goes here
            updatedVersion = 2
        }

        /*if(updatedVersion == 2) {
            ...
            updatedVersion = 3
        }*/

        return updatedVersion
    }

    fun getNewestActivity(): UserActivity? {
        if(items.size == 0)
            return null

        var newestActivity = items[items.size - 1]
        for(userActivity in items) {
            if(userActivity.startTime.isAfter(newestActivity.startTime))
                newestActivity = userActivity
        }

        return newestActivity
    }

}