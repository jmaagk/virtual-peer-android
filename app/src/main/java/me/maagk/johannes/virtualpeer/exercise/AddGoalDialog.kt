package me.maagk.johannes.virtualpeer.exercise

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.textfield.TextInputEditText
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.goals.Goal
import me.maagk.johannes.virtualpeer.goals.GoalStorage
import me.maagk.johannes.virtualpeer.useractivity.UserActivity
import java.time.LocalDate

class AddGoalDialog(context: Context) : AlertDialog(context) {

    interface OnGoalCompletedListener {
        fun onGoalCompleted(goal: Goal)
    }

    lateinit var onGoalCompletedListener: OnGoalCompletedListener

    private var selectedDate: LocalDate? = null

    init {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)

        val matrixRadioGroup: RadioGroup = dialogView.findViewById(R.id.matrixPositionRadioGroup)
        for(child in matrixRadioGroup.children) {
            val position = when(child.id) {
                R.id.radioButtonUrgentImportant -> EisenhowerMatrix.Position.URGENT_IMPORTANT
                R.id.radioButtonNotUrgentImportant -> EisenhowerMatrix.Position.NOT_URGENT_IMPORTANT
                R.id.radioButtonUrgentNotImportant -> EisenhowerMatrix.Position.URGENT_NOT_IMPORTANT
                else -> EisenhowerMatrix.Position.NOT_URGENT_NOT_IMPORTANT
            }
            val color = position.getColor(context)

            val drawable = ContextCompat.getDrawable(context, R.drawable.color_radio_button) ?: continue

            /*
             * setting the correct colors here; this is a bit complicated:
             * The real drawable is actually transparent except for a ring whenever it's checked.
             * DST_OVER then paints this destination (the ring) over the source (just the color as a circle)
             */
            drawable.setTint(color)
            drawable.setTintMode(PorterDuff.Mode.DST_OVER)

            child.background = drawable
        }

        val activityAreaRadioGroup: RadioGroup = dialogView.findViewById(R.id.activityPoolRadioGroup)
        for(child in activityAreaRadioGroup.children) {
            if(child !is RadioButton)
                continue

            val type = when(child.id) {
                R.id.radioButtonWork -> UserActivity.Type.POOL_WORK
                R.id.radioButtonEssential -> UserActivity.Type.POOL_ESSENTIAL
                else -> UserActivity.Type.POOL_REWARDS
            }
            val color = type.getColor(context)

            // the two states this view can be in (in terms of colors)
            val states = Array(2) {
                IntArray(1)
            }
            states[0][0] = android.R.attr.state_checked
            states[1][0] = android.R.attr.state_enabled

            // the two colors the text will have depending on the state
            val textColors = IntArray(2)
            textColors[0] = Utils.getColor(context, android.R.color.white)
            textColors[1] = color

            val textColorStateList = ColorStateList(states, textColors)
            child.setTextColor(textColorStateList)

            val drawable = ContextCompat.getDrawable(context, R.drawable.activity_pool_radio_button) ?: continue

            // the two colors the drawable (the actual button) will have depending on the state
            val drawableColors = IntArray(2)
            drawableColors[0] = color
            drawableColors[1] = Utils.getColor(context, android.R.color.white)

            val drawableColorStateList = ColorStateList(states, drawableColors)

            // see other RadioGroup for details
            drawable.setTintMode(PorterDuff.Mode.DST_OVER)
            drawable.setTintList(drawableColorStateList)

            child.background = drawable
        }

        // selecting some default values to avoid error messages when something isn't selected
        matrixRadioGroup.check(R.id.radioButtonUrgentImportant)
        activityAreaRadioGroup.check(R.id.radioButtonWork)

        val goalCheckBox: CheckBox = dialogView.findViewById(R.id.goalCheckBox)

        val selectDeadlineButton: ImageView = dialogView.findViewById(R.id.selectDeadlineButton)
        selectDeadlineButton.setOnClickListener {
            val onDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)

                selectedDate?.let {
                    if(it.isBefore(LocalDate.now())) {
                        selectedDate = null
                        Toast.makeText(context, R.string.eisenhower_matrix_add_goal_dialog_invalid_deadline, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val dateNow = LocalDate.now()
            val datePickerDialog = DatePickerDialog(context, onDateSetListener, dateNow.year, dateNow.monthValue - 1, dateNow.dayOfMonth)
            datePickerDialog.show()
        }

        val goalNameInput: TextInputEditText = dialogView.findViewById(R.id.goalNameInput)
        goalNameInput.setOnEditorActionListener start@ { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                if(goalNameInput.text.isNullOrBlank()) {
                    goalNameInput.error = context.getString(R.string.eisenhower_matrix_add_goal_dialog_error_empty_name)
                    return@start false
                } else {
                    goalNameInput.error = null
                    dismiss()
                }

                val storage = GoalStorage(context)

                val position = when(matrixRadioGroup.checkedRadioButtonId) {
                    R.id.radioButtonUrgentImportant -> EisenhowerMatrix.Position.URGENT_IMPORTANT
                    R.id.radioButtonNotUrgentImportant -> EisenhowerMatrix.Position.NOT_URGENT_IMPORTANT
                    R.id.radioButtonUrgentNotImportant -> EisenhowerMatrix.Position.URGENT_NOT_IMPORTANT
                    else -> EisenhowerMatrix.Position.NOT_URGENT_NOT_IMPORTANT
                }

                val activityArea = when(activityAreaRadioGroup.checkedRadioButtonId) {
                    R.id.radioButtonWork -> UserActivity.Type.POOL_WORK
                    R.id.radioButtonEssential -> UserActivity.Type.POOL_ESSENTIAL
                    else -> UserActivity.Type.POOL_REWARDS
                }

                val goal = Goal(storage.generateNewId(), goalNameInput.text.toString(), goalCheckBox.isChecked, selectedDate, position, activityArea)
                if(::onGoalCompletedListener.isInitialized)
                    onGoalCompletedListener.onGoalCompleted(goal)

                return@start true
            }

            false
        }

        setView(dialogView)
    }

}