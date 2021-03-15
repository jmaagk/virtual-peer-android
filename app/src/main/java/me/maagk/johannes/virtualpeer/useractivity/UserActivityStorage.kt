package me.maagk.johannes.virtualpeer.useractivity

import android.content.Context
import android.net.Uri
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.ArrayList

class UserActivityStorage(private val context: Context, refresh: Boolean = true) {

    private val FILE_NAME = "activities.xml"
    private val VERSION = 1

    val userActivities = ArrayList<UserActivity>()
    lateinit var timeZone: ZoneId

    private val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy")
    private val activitiesFile = File(context.filesDir, FILE_NAME)

    init {
        // refreshing the internal list of activities if the user of this class wants it to happen
        if(refresh)
            refresh()

        // making sure the "files" directory in the app's directory exists
        if(!context.filesDir.exists()) {
            val success = context.filesDir.mkdir()
            if(!success)
                TODO("add some error handling here (maybe the app should quit here?)")
        }
    }

    fun refresh() {
        if(!activitiesFile.exists())
            return

        // clearing the current list of activities as these will be loaded here
        userActivities.clear()

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(context.contentResolver.openInputStream(Uri.fromFile(activitiesFile)))

        val activitiesTag = doc.documentElement
        if(activitiesTag.tagName != "activities") {
            // TODO: some error handling might be needed
        }

        val fileVersion = activitiesTag.getAttribute("version").toInt()
        timeZone = ZoneId.of(activitiesTag.getAttribute("timeZone"))

        val activityTags = activitiesTag.getElementsByTagName("activity")
        for(i in 0 until activityTags.length) {
            val activityTag = activityTags.item(i)
            val activity = parseActivity(activityTag, fileVersion)
            userActivities.add(activity)
        }

        if(fileVersion != VERSION) {
            update(fileVersion)
            save()
        }
    }

    private fun parseActivity(activityTag: Node, version: Int): UserActivity {
        val type = UserActivity.Type.valueOf(activityTag.attributes.getNamedItem("type").nodeValue.toUpperCase(Locale.ROOT))

        val startTime = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(
                        activityTag.attributes.getNamedItem("startTime").nodeValue.toLong()),
                        timeZone)

        val endTimeLong = activityTag.attributes.getNamedItem("endTime").nodeValue.toLong()
        val endTime = if(endTimeLong == -1L) {
            null
        } else {
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(endTimeLong), timeZone)
        }

        val activity = UserActivity(type, startTime, endTime)

        val userRatingTypeTag = activityTag.attributes.getNamedItem("userRatingType")
        val userRatingTag = activityTag.attributes.getNamedItem("userRating")
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

    fun save() {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        doc.xmlStandalone = true

        val root = doc.createElement("activities")
        root.setAttribute("version", VERSION.toString())
        root.setAttribute("timeZone", ZoneId.systemDefault().id)

        for(userActivity in userActivities)
            root.appendChild(convertActivityToXml(userActivity, doc))

        doc.appendChild(root)

        val transformer = TransformerFactory.newInstance().newTransformer()
        // some options to make the resulting files more readable
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        val input = DOMSource(doc)
        val output = StreamResult(activitiesFile)

        transformer.transform(input, output)
    }

    private fun convertActivityToXml(activity: UserActivity, doc: Document): Element {
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

    private fun update(fromVersion: Int) {
        // just a quick move to a new variable as arguments can't be reassigned in Kotlin
        var updatedVersion = fromVersion

        /* some code to update the version we're now on
         * switch without break would usually be used here to "fall" through necessary
         * updates consecutively but it doesn't exist in Kotlin and when doesn't do the same thing */
        if(updatedVersion == 1) {
            // future update code goes here
            updatedVersion = 2
        }

        /*if(updatedVersion == 2) {
            ...
            updatedVersion = 3
        }*/

        if(updatedVersion == VERSION)
            save() // saving user activities; this will make the changes made by the update persistent
        else
            TODO("Add error handling for failed updates")
    }

    fun getNewestActivity(): UserActivity? {
        if(userActivities.size == 0)
            return null

        var newestActivity = userActivities[userActivities.size - 1]
        for(userActivity in userActivities) {
            if(userActivity.startTime.isAfter(newestActivity.startTime))
                newestActivity = userActivity
        }

        return newestActivity
    }

}