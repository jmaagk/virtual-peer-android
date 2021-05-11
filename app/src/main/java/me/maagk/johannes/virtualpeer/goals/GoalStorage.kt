package me.maagk.johannes.virtualpeer.goals

import android.content.Context
import me.maagk.johannes.virtualpeer.Storage
import me.maagk.johannes.virtualpeer.exercise.EisenhowerMatrix
import me.maagk.johannes.virtualpeer.useractivity.UserActivity
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class GoalStorage(context: Context, refresh: Boolean = true): Storage<Goal>(context, refresh) {

    companion object {
        const val ID_LENGTH = 32
    }

    override val FILE_NAME: String
        get() = "goals.xml"

    override val VERSION: Int
        get() = 1

    // just an alias to have the name make a bit more sense
    val goals = items

    override fun refreshList(doc: Document) {
        val goalsTag = doc.documentElement
        if(goalsTag.tagName != "goals")
            return

        val fileVersion = goalsTag.getAttribute("version").toInt()

        val goalTags = goalsTag.getElementsByTagName("goal")
        for(i in 0 until goalTags.length) {
            val goalTag = goalTags.item(i)
            val goal = parseItem(goalTag, fileVersion)
            items.add(goal)
        }
    }

    override fun parseItem(tag: Node, version: Int): Goal {
        val attributes = tag.attributes

        val id = attributes.getNamedItem("id").nodeValue
        val name = attributes.getNamedItem("name").nodeValue
        val completed = attributes.getNamedItem("completed").nodeValue.toBoolean()

        val deadlineLong = attributes.getNamedItem("deadline").nodeValue.toLong()
        val deadline = if(deadlineLong == -1L) {
            null
        } else {
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(deadlineLong), ZoneId.systemDefault()).toLocalDate()
        }

        val eisenhowerMatrixPosition = EisenhowerMatrix.Position.valueOf(attributes.getNamedItem("eisenhowerMatrixPosition").nodeValue)
        val activityType = UserActivity.Type.valueOf(attributes.getNamedItem("activityType").nodeValue)
        val pinned = attributes.getNamedItem("pinned").nodeValue.toBoolean()

        return Goal(id, name, completed, deadline, eisenhowerMatrixPosition, activityType, pinned)
    }

    override fun getRootElement(doc: Document): Element = doc.createElement("goals")

    override fun convertItemToXml(item: Goal, doc: Document): Element {
        val goal = item

        val goalRoot = doc.createElement("goal")

        goalRoot.setAttribute("id", goal.id)
        goalRoot.setAttribute("name", goal.name)
        goalRoot.setAttribute("completed", goal.completed.toString())

        val deadlineString = if(goal.hasDeadline()) {
            goal.deadline!!.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toString()
        } else {
            (-1).toString()
        }
        goalRoot.setAttribute("deadline", deadlineString)

        goalRoot.setAttribute("eisenhowerMatrixPosition", goal.position.toString())
        goalRoot.setAttribute("activityType", goal.activityArea.toString())
        goalRoot.setAttribute("pinned", goal.pinned.toString())

        return goalRoot
    }

    fun generateNewId(): String {
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = upper.toLowerCase(Locale.ROOT)
        val numbers = "0123456789"
        val charset = upper + lower + numbers

        val random = Random()

        fun generate(): String {
            val output = StringBuilder()
            for(i in 0 until ID_LENGTH)
                output.append(charset[random.nextInt(charset.length)])
            return output.toString()
        }

        var output = ""
        do {
            output = generate()
        } while(!validateId(output))

        return output
    }

    fun validateId(id: String): Boolean {
        for(goal in items) {
            if(id == goal.id)
                return false
        }
        return true
    }

    override fun updateInternal(fromVersion: Int): Int {
        var updatedVersion = fromVersion

        /*if(updatedVersion == 1) {
            ...
            updatedVersion = 2
        }*/

        return updatedVersion
    }

    fun deleteGoal(toDelete: Goal) {
        for(goal in items) {
            if(goal.id == toDelete.id) {
                items .remove(goal)
                break
            }
        }
    }

}