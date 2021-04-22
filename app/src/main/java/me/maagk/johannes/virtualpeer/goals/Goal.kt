package me.maagk.johannes.virtualpeer.goals

import me.maagk.johannes.virtualpeer.exercise.EisenhowerMatrix
import me.maagk.johannes.virtualpeer.useractivity.UserActivity
import java.time.LocalDate

data class Goal(val id: String, val name: String, var completed: Boolean, val deadline: LocalDate?, val position: EisenhowerMatrix.Position, val activityArea: UserActivity.Type, var pinned: Boolean = false)