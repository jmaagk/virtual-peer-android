package me.maagk.johannes.virtualpeer.goals

import me.maagk.johannes.virtualpeer.exercise.EisenhowerMatrix
import me.maagk.johannes.virtualpeer.useractivity.UserActivity
import java.time.ZonedDateTime

data class Goal(val id: String, val name: String, var completed: Boolean, val deadline: ZonedDateTime?, val position: EisenhowerMatrix.Position, val activityArea: UserActivity.Type, var pinned: Boolean = false)