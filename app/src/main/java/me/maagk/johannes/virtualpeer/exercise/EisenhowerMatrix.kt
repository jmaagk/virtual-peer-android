package me.maagk.johannes.virtualpeer.exercise

import android.content.Context
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils

class EisenhowerMatrix {

    enum class Position {
        URGENT_IMPORTANT, NOT_URGENT_IMPORTANT, URGENT_NOT_IMPORTANT, NOT_URGENT_NOT_IMPORTANT;

        fun getTitle(context: Context): String {
            return when(this) {
                URGENT_IMPORTANT -> context.getString(R.string.eisenhower_matrix_urgent_important)
                NOT_URGENT_IMPORTANT -> context.getString(R.string.eisenhower_matrix_not_urgent_important)
                URGENT_NOT_IMPORTANT -> context.getString(R.string.eisenhower_matrix_urgent_not_important)
                NOT_URGENT_NOT_IMPORTANT -> context.getString(R.string.eisenhower_matrix_not_urgent_not_important)
            }
        }

        fun getColor(context: Context): Int {
            return when(this) {
                URGENT_IMPORTANT -> Utils.getColor(context, R.color.colorTaskUrgentImportant)
                NOT_URGENT_IMPORTANT -> Utils.getColor(context, R.color.colorTaskNotUrgentImportant)
                URGENT_NOT_IMPORTANT -> Utils.getColor(context, R.color.colorTaskUrgentNotImportant)
                NOT_URGENT_NOT_IMPORTANT -> Utils.getColor(context, R.color.colorTaskNotUrgentNotImportant)
            }
        }
    }

}