package me.maagk.johannes.virtualpeer.pins

import android.content.Context
import me.maagk.johannes.virtualpeer.Storage
import me.maagk.johannes.virtualpeer.exercise.Exercise
import me.maagk.johannes.virtualpeer.exercise.ExerciseStorage
import me.maagk.johannes.virtualpeer.goals.Goal
import me.maagk.johannes.virtualpeer.goals.GoalStorage
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.util.*

class PinStorage (context: Context,
                             val goalStorage: GoalStorage = GoalStorage(context),
                             val exerciseStorage: ExerciseStorage = ExerciseStorage(context)
)
    : Storage<Pin>(context, false) {

    val pins = items

    init {
        // refreshing here to allow goalStorage and exerciseStorage to be initialized
        refresh()
    }

    override val FILE_NAME: String
        get() = "pins.xml"

    override val VERSION: Int
        get() = 1

    override fun refresh() {
        refresh(true)
    }

    fun refresh(refreshOtherLists: Boolean) {
        if(refreshOtherLists) {
            goalStorage.refresh()
            exerciseStorage.refresh()
        }

        super.refresh()

        // adding new pins that aren't part of the list yet
        for(goal in goalStorage.goals) {
            if(goal.pinned && !containsGoal(goal))
                items.add(GoalPin(Pin.Size.NORMAL, goal, context))
        }

        for(exercise in exerciseStorage.exercises) {
            if(exercise.pinned && !containsExercise(exercise))
                items.add(ExercisePin(Pin.Size.NORMAL, exercise))
        }
    }

    override fun refreshList(doc: Document) {
        val pinsTag = doc.documentElement
        if(pinsTag.tagName != "pins")
            return

        val fileVersion = pinsTag.getAttribute("version").toInt()

        val pinTags = pinsTag.getElementsByTagName("pin")
        for(i in 0 until pinTags.length) {
            val pinTag = pinTags.item(i)
            val pin = parseItem(pinTag, fileVersion)

            // not adding empty pins here; in case a goal was deleted
            if(pin !is Pin.EmptyPin)
                items.add(pin)
        }
    }

    override fun getRootElement(doc: Document): Element = doc.createElement("pins")

    override fun parseItem(tag: Node, version: Int): Pin {
        val size = Pin.Size.valueOf(tag.attributes.getNamedItem("size").nodeValue.toUpperCase(Locale.ROOT))

        val goalIdAttr = tag.attributes.getNamedItem("goalId")
        val exerciseAttr = tag.attributes.getNamedItem("exercise")

        if(goalIdAttr != null) {
            val goalId = goalIdAttr.nodeValue
            val goal = goalStorage.findGoalById(goalId)

            // only returning a valid pin if the goal is found (still exists) and is still pinned
            if(goal != null && goal.pinned)
                return GoalPin(size, goal, context)
        } else if(exerciseAttr != null) {
            val exerciseInternalName = exerciseAttr.nodeValue
            val exercise = exerciseStorage.getExerciseByInternalName(exerciseInternalName)

            // only returning a valid pin if the exercise is found and still pinned
            if(exercise != null && exercise.pinned)
                return ExercisePin(size, exercise)
        }

        return Pin.EmptyPin()
    }

    override fun convertItemToXml(item: Pin, doc: Document): Element {
        val pinRoot = doc.createElement("pin")

        pinRoot.setAttribute("size", item.size.toString())

        if(item is GoalPin) {
            pinRoot.setAttribute("goalId", item.goal.id)
        } else if(item is ExercisePin) {
            pinRoot.setAttribute("exercise", item.exercise.internalName)
        }

        return pinRoot
    }

    // no update code just yet
    override fun updateInternal(fromVersion: Int): Int = fromVersion

    private fun containsGoal(goal: Goal): Boolean {
        for(pin in pins) {
            if(pin is GoalPin && pin.goal.id == goal.id)
                return true
        }

        return false
    }

    private fun containsExercise(exercise: Exercise): Boolean {
        for(pin in pins) {
            if(pin is ExercisePin && pin.exercise.internalName == exercise.internalName)
                return true
        }

        return false
    }

}