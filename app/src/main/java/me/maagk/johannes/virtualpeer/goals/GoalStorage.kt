package me.maagk.johannes.virtualpeer.goals

import android.content.Context
import android.net.Uri
import me.maagk.johannes.virtualpeer.exercise.EisenhowerMatrix
import me.maagk.johannes.virtualpeer.useractivity.UserActivity
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.ArrayList

class GoalStorage(private val context: Context, refresh: Boolean = true) {

    companion object {
        const val ID_LENGTH = 32
    }

    private val FILE_NAME = "goals.xml"
    private val VERSION = 1

    val goals = ArrayList<Goal>()

    private val file = File(context.filesDir, FILE_NAME)

    init {
        if(refresh)
            refresh()

        if(!context.filesDir.exists()) {
            val success = context.filesDir.mkdir()
            if(!success)
                TODO("Error handling: can't save goals when files directory doesn't exist and can't be created")
        }
    }

    fun refresh() {
        if(!file.exists())
            return

        goals.clear()

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(context.contentResolver.openInputStream(Uri.fromFile(file)))

        val goalsTag = doc.documentElement
        if(goalsTag.tagName != "goals")
            return

        val fileVersion = goalsTag.getAttribute("version").toInt()

        val goalTags = goalsTag.getElementsByTagName("goal")
        for(i in 0 until goalTags.length) {
            val goalTag = goalTags.item(i)
            val goal = parseGoal(goalTag, fileVersion)
            goals.add(goal)
        }

        if(fileVersion != VERSION)
            update(fileVersion)
    }

    private fun parseGoal(goalTag: Node, version: Int): Goal {
        val attributes = goalTag.attributes

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

    fun save() {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        doc.xmlStandalone = true

        val root = doc.createElement("goals")
        root.setAttribute("version", VERSION.toString())

        for(goal in goals)
            root.appendChild(convertGoalToXml(goal, doc))

        doc.appendChild(root)

        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        val input = DOMSource(doc)
        val output = StreamResult(file)

        transformer.transform(input, output)
    }

    private fun convertGoalToXml(goal: Goal, doc: Document): Element {
        val goalRoot = doc.createElement("goal")

        goalRoot.setAttribute("id", goal.id)
        goalRoot.setAttribute("name", goal.name)
        goalRoot.setAttribute("completed", goal.completed.toString())

        val deadlineString = if(goal.deadline == null) {
            (-1).toString()
        } else {
            goal.deadline.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toString()
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
        for(goal in goals) {
            if(id == goal.id)
                return false
        }
        return true
    }

    private fun update(fromVersion: Int) {
        var updatedVersion = fromVersion

        /*if(updatedVersion == 1) {
            ...
            updatedVersion = 2
        }*/

        if(updatedVersion == VERSION)
            save()
        else
            TODO("Add error handling for failed updates")
    }

    fun deleteGoal(toDelete: Goal) {
        for(goal in goals) {
            if(goal.id == toDelete.id) {
                goals.remove(goal)
                break
            }
        }
    }

}