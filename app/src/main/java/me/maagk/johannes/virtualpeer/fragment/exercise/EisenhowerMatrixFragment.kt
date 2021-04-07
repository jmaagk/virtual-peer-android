package me.maagk.johannes.virtualpeer.fragment.exercise

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.exercise.EisenhowerMatrix
import me.maagk.johannes.virtualpeer.useractivity.UserActivity

class EisenhowerMatrixFragment : Fragment(R.layout.fragment_eisenhower_matrix) {

    companion object {
        const val TAG = "eisenhowerMatrix"
    }

    // TODO: add ability to add deadline
    private class AddGoalDialog(context: Context) : AlertDialog(context) {

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

            val goalNameInput: TextInputEditText = dialogView.findViewById(R.id.goalNameInput)
            goalNameInput.setOnEditorActionListener start@ { _, actionId, _ ->
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    if(goalNameInput.text.isNullOrBlank()) {
                        goalNameInput.error = context.getString(R.string.eisenhower_matrix_add_goal_dialog_error_empty_name)
                    } else {
                        goalNameInput.error = null
                        dismiss()
                        return@start true
                    }

                    // TODO: create goal instance here
                }

                false
            }

            setView(dialogView)
        }

    }

    private class MatrixPart(val rootLayout: LinearLayout, val position: EisenhowerMatrix.Position) {
        val context = rootLayout.context
        val titleText: TextView = rootLayout.findViewById(R.id.eisenhowerMatrixPositionTitle)
        val goalListCard: CardView = rootLayout.findViewById(R.id.goalListCard)
        val goalList: RecyclerView = goalListCard.findViewById(R.id.goalList)

        init {
            // setting the background color of this part
            rootLayout.setBackgroundColor(position.getColor(context))

            // adjusting the position of this part's list
            // 2 of these have the majority of their padding on the left, the others on the right
            val marginLarge = Utils.dpToPx(35f, context.resources.displayMetrics)
            val marginSmall = Utils.dpToPx(2.5f, context.resources.displayMetrics)

            val layoutParams = goalListCard.layoutParams as LinearLayout.LayoutParams

            when(position) {
                EisenhowerMatrix.Position.URGENT_IMPORTANT, EisenhowerMatrix.Position.URGENT_NOT_IMPORTANT -> {
                    layoutParams.marginStart = marginLarge
                    layoutParams.marginEnd = marginSmall
                }

                EisenhowerMatrix.Position.NOT_URGENT_IMPORTANT, EisenhowerMatrix.Position.NOT_URGENT_NOT_IMPORTANT -> {
                    layoutParams.marginStart = marginSmall
                    layoutParams.marginEnd = marginLarge
                }
            }

            goalListCard.layoutParams = layoutParams

            titleText.text = position.getTitle(context)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val urgentImportantPart = MatrixPart(view.findViewById(R.id.urgentImportantLayout), EisenhowerMatrix.Position.URGENT_IMPORTANT)
        val notUrgentImportantPart = MatrixPart(view.findViewById(R.id.notUrgentImportantLayout), EisenhowerMatrix.Position.NOT_URGENT_IMPORTANT)
        val urgentNotImportantPart = MatrixPart(view.findViewById(R.id.urgentNotImportantLayout), EisenhowerMatrix.Position.URGENT_NOT_IMPORTANT)
        val notUrgentNotImportantPart = MatrixPart(view.findViewById(R.id.notUrgentNotImportantLayout), EisenhowerMatrix.Position.NOT_URGENT_NOT_IMPORTANT)

        val addGoalButton: FloatingActionButton = view.findViewById(R.id.addGoal)
        addGoalButton.setOnClickListener {
            val dialog = AddGoalDialog(requireContext())
            dialog.show()
        }
    }

}