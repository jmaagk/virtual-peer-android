package me.maagk.johannes.virtualpeer.pins

import android.content.Context
import me.maagk.johannes.virtualpeer.goals.Goal

class GoalPin(size: Size, val goal: Goal, context: Context? = null) : Pin(size) {

    init {
        if(context != null)
            color = goal.activityArea.getColor(context)
    }

}