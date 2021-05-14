package me.maagk.johannes.virtualpeer.fragment.start

import android.content.Context
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.Utils.Companion.getFormattedDate
import me.maagk.johannes.virtualpeer.pins.ExercisePin
import me.maagk.johannes.virtualpeer.pins.GoalPin
import me.maagk.johannes.virtualpeer.pins.Pin

class PinListAdapter(val context: Context, val pins: MutableList<Pin>) : RecyclerView.Adapter<PinListAdapter.PinViewHolder>() {

    private val VIEW_TYPE_SMALL = 0
    private val VIEW_TYPE_NORMAL = 1
    private val VIEW_TYPE_LARGE = 2

    abstract inner class PinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val pinCard: CardView = itemView.findViewById(R.id.pinCard)
        val background: ImageView = pinCard.findViewById(R.id.background)
        val pinIcon: ImageView = pinCard.findViewById(R.id.pinIcon)

        var tintNeeded = false

        open fun bind(pin: Pin) {
            pinCard.setCardBackgroundColor(pin.color)

            val isGoal = pin is GoalPin
            background.visibility = if(isGoal) View.VISIBLE else View.GONE

            tintNeeded = pin.isTintNeeded(context)
            if(tintNeeded)
                pinIcon.setColorFilter(pin.getColorOnBackground(context))

            if(pin is ExercisePin) {
                bindExercisePin(pin)
            } else {
                bindGoalPin(pin as GoalPin)
            }
        }

        abstract fun bindExercisePin(pin: ExercisePin)

        abstract fun bindGoalPin(pin: GoalPin)

    }

    inner class SmallPinViewHolder(itemView: View) : PinViewHolder(itemView) {

        val pinText: TextView = itemView.findViewById(R.id.pinText)

        override fun bind(pin: Pin) {
            super.bind(pin)

            val isGoal = pin is GoalPin
            pinIcon.visibility = if(isGoal) View.GONE else View.VISIBLE
            pinText.visibility = if(isGoal) View.VISIBLE else View.GONE
        }

        override fun bindExercisePin(pin: ExercisePin) {
            pinIcon.setImageDrawable(pin.exercise.getIcon())
        }

        override fun bindGoalPin(pin: GoalPin) {
            pinText.text = pin.goal.activityArea.getName(context)[0].toString()

            // TODO: this could cause problems because texts are tinted once but not reset; testing required
            if(tintNeeded)
                pinText.setTextColor(pin.getColorOnBackground(context))
        }

    }

    open inner class NormalPinViewHolder(itemView: View) : PinViewHolder(itemView) {

        // exercise pins
        val exercisePinLayout: ViewGroup = itemView.findViewById(R.id.exercisePinLayout)
        val lastActivityText: TextView = exercisePinLayout.findViewById(R.id.lastActivityText)
        val lastActivityTimeText: TextView = exercisePinLayout.findViewById(R.id.lastActivityTimeText)

        // goal pins
        val goalPinLayout: ViewGroup = itemView.findViewById(R.id.goalPinLayout)
        val activityAreaText: TextView = goalPinLayout.findViewById(R.id.activityAreaName)
        val deadlineText: TextView = goalPinLayout.findViewById(R.id.deadline)
        val goalNameText: TextView = goalPinLayout.findViewById(R.id.goalName)
        val goalPositionText: TextView = goalPinLayout.findViewById(R.id.goalPosition)

        override fun bind(pin: Pin) {
            super.bind(pin)

            val isGoal = pin is GoalPin
            exercisePinLayout.visibility = if(isGoal) View.GONE else View.VISIBLE
            goalPinLayout.visibility = if(isGoal) View.VISIBLE else View.GONE

            // setting all text colors if it's needed
            if(tintNeeded) {
                val textColor = pin.getColorOnBackground(context)

                lastActivityText.setTextColor(textColor)
                lastActivityTimeText.setTextColor(textColor)
                activityAreaText.setTextColor(textColor)
                deadlineText.setTextColor(textColor)
                goalNameText.setTextColor(textColor)
                goalPositionText.setTextColor(textColor)
            }
        }

        override fun bindExercisePin(pin: ExercisePin) {
            pinIcon.setImageDrawable(pin.exercise.getIcon())
            lastActivityTimeText.text = pin.exercise.getLastStartTimeText()
        }

        override fun bindGoalPin(pin: GoalPin) {
            activityAreaText.text = pin.goal.activityArea.getName(context)

            val backgroundDrawable = activityAreaText.background as LayerDrawable
            val activityAreaColor = backgroundDrawable.getDrawable(0)
            activityAreaColor.setTint(pin.goal.activityArea.getColor(context))
            val strokeColor = backgroundDrawable.getDrawable(1)
            strokeColor.setTint(Utils.getColor(context, R.color.colorBackground))

            deadlineText.visibility = if(pin.goal.hasDeadline()) View.VISIBLE else View.GONE
            if(pin.goal.hasDeadline())
                deadlineText.text = pin.goal.deadline!!.getFormattedDate(context)

            // TODO: ellipsize this text
            goalNameText.text = pin.goal.name

            goalPositionText.text = pin.goal.position.getTitle(context)
        }

    }

    inner class LargePinViewHolder(itemView: View) : NormalPinViewHolder(itemView)

    private lateinit var layoutInflater: LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinViewHolder {
        if(!::layoutInflater.isInitialized)
            layoutInflater = LayoutInflater.from(parent.context)

        return when(viewType) {
            VIEW_TYPE_SMALL -> {
                val view = layoutInflater.inflate(R.layout.view_pinned_item_small, parent, false)
                SmallPinViewHolder(view)
            }

            VIEW_TYPE_NORMAL -> {
                val view = layoutInflater.inflate(R.layout.view_pinned_item_normal, parent, false)
                NormalPinViewHolder(view)
            }

            else -> {
                val view = layoutInflater.inflate(R.layout.view_pinned_item_large, parent, false)
                LargePinViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: PinViewHolder, position: Int) = holder.bind(pins[position])

    override fun getItemCount(): Int = pins.size

    override fun getItemViewType(position: Int): Int {
        return when(pins[position].size) {
            Pin.Size.SMALL -> VIEW_TYPE_SMALL
            Pin.Size.NORMAL -> VIEW_TYPE_NORMAL
            Pin.Size.LARGE -> VIEW_TYPE_LARGE
        }
    }

}