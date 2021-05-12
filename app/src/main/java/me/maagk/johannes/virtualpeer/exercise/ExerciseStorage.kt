package me.maagk.johannes.virtualpeer.exercise

import android.content.Context
import me.maagk.johannes.virtualpeer.Storage
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ExerciseStorage(context: Context) : Storage<Exercise>(context) {

    override val FILE_NAME: String
        get() = "exercises.xml"
    override val VERSION: Int
        get() = 1

    val exercises = items

    init {
        // add all exercises in case the list is empty
        if(items.isEmpty()) {
            items.add(PomodoroExercise(context))
            items.add(BoxBreathingExercise(context))
            items.add(MeditationExercise(context))
        }
    }

    override fun refreshList(doc: Document) {
        val exercisesTag = doc.documentElement
        if(exercisesTag.tagName != "exercises")
            return

        val fileVersion = exercisesTag.getAttribute("version").toInt()

        val exerciseTags = exercisesTag.getElementsByTagName("exercise")
        for(i in 0 until exerciseTags.length) {
            val exerciseTag = exerciseTags.item(i)
            val exercise = parseItem(exerciseTag, fileVersion)
            items.add(exercise)
        }
    }

    override fun getRootElement(doc: Document): Element = doc.createElement("exercises")

    override fun parseItem(tag: Node, version: Int): Exercise {
        val pomodoroExercise = PomodoroExercise(context)
        val boxBreathingExercise = BoxBreathingExercise(context)
        val meditationExercise = MeditationExercise(context)

        val exercise = when(tag.attributes.getNamedItem("name").nodeValue) {
            pomodoroExercise.internalName -> pomodoroExercise
            boxBreathingExercise.internalName -> boxBreathingExercise
            else -> meditationExercise
        }

        val totalTimeAttr = tag.attributes.getNamedItem("totalTime")
        if(totalTimeAttr != null)
            exercise.totalTimeMillis = totalTimeAttr.nodeValue.toLong()

        val lastActivityTimeAttr = tag.attributes.getNamedItem("lastStartTime")
        if(lastActivityTimeAttr != null)
            exercise.lastStartTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastActivityTimeAttr.nodeValue.toLong()), ZoneId.systemDefault())

        val runningAttr = tag.attributes.getNamedItem("running")
        if(runningAttr != null)
            exercise.running = runningAttr.nodeValue.toBoolean()

        return exercise
    }

    override fun convertItemToXml(item: Exercise, doc: Document): Element {
        val exercise = item

        val exerciseRoot = doc.createElement("exercise")
        exerciseRoot.setAttribute("name", exercise.internalName)

        if(exercise.totalTimeMillis >= 0)
            exerciseRoot.setAttribute("totalTime", exercise.totalTimeMillis.toString())

        if(exercise.hasLastStartTime())
            exerciseRoot.setAttribute("lastStartTime", exercise.lastStartTime.toInstant().toEpochMilli().toString())

        if(exercise.running)
            exerciseRoot.setAttribute("running", exercise.running.toString())

        return exerciseRoot
    }

    override fun updateInternal(fromVersion: Int): Int {
        var updatedVersion = fromVersion

        /*if(updatedVersion == 1) {
            ...
            updatedVersion = 2
        }*/

        return updatedVersion
    }

    fun editExercise(exercise: Exercise) {
        exercises.forEachIndexed { index, e ->
            if(e.internalName == exercise.internalName) {
                exercises[index] = exercise
                return@forEachIndexed
            }
        }
    }

    inline fun <reified T : Exercise> getExercise(): T {
        for(exercise in exercises) {
            if(exercise is T)
                return exercise
        }

        TODO("Exercise not implemented")
    }

    inline fun <reified T : Exercise> notifyExerciseStart() {
        // ending any exercises that are still running
        for(exercise in exercises) {
            if(exercise.running)
                notifyExerciseEnd(exercise)
        }

        val exercise = getExercise<T>()
        exercise.lastStartTime = ZonedDateTime.now()
        exercise.running = true
        editExercise(exercise)
    }

    inline fun <reified T : Exercise> notifyExerciseEnd() {
        val exercise = getExercise<T>()
        notifyExerciseEnd(exercise)
    }

    fun notifyExerciseEnd(e: Exercise) {
        var exercise = e
        for(ex in exercises) {
            if(ex.internalName == e.internalName) {
                exercise = ex
                break
            }
        }

        if(!exercise.running)
            return

        if(exercise.totalTimeMillis == -1L)
            exercise.totalTimeMillis = 0

        exercise.totalTimeMillis += System.currentTimeMillis() - exercise.lastStartTime.toInstant().toEpochMilli()
        exercise.running = false

        editExercise(exercise)
    }

}