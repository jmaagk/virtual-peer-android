package me.maagk.johannes.virtualpeer.fragment.start

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.LayerDrawable
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.cardview.widget.CardView
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.Utils.Companion.getFormattedDate
import me.maagk.johannes.virtualpeer.pins.ExercisePin
import me.maagk.johannes.virtualpeer.pins.GoalPin
import me.maagk.johannes.virtualpeer.pins.Pin
import me.maagk.johannes.virtualpeer.pins.PinStorage

class PinListAdapter(
    val activity: AppCompatActivity,
    val context: Context = activity.applicationContext,
    val pinStorage: PinStorage) :
    RecyclerView.Adapter<PinListAdapter.PinViewHolder>() {

    private val VIEW_TYPE_SMALL = 0
    private val VIEW_TYPE_NORMAL = 1
    private val VIEW_TYPE_LARGE = 2

    var changeSizeActionMode: ActionMode? = null

    private var inChangeSizeActionMode = false
    private var changeSizeActionModeSubjectIndex = -1

    private var recyclerView: RecyclerView? = null

    abstract inner class PinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        val pinCard: CardView = itemView.findViewById(R.id.pinCard)
        val background: ImageView = pinCard.findViewById(R.id.background)
        val pinIcon: ImageView = pinCard.findViewById(R.id.pinIcon)

        var tintNeeded = false

        var prevSize: Pin.Size? = null

        lateinit var currentPin: Pin

        init {
            pinCard.setOnClickListener(this)
            pinCard.setOnLongClickListener(this)
        }

        open fun bind(pin: Pin) {
            currentPin = pin

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

        override fun onClick(view: View?) {
            if(inChangeSizeActionMode) {
                if(changeSizeActionModeSubjectIndex != adapterPosition)
                    return

                val currentSizeIndex = Pin.Size.values().indexOf(currentPin.size)
                val newSizeIndex = if(currentSizeIndex == Pin.Size.values().size - 1) 0 else currentSizeIndex + 1

                currentPin.size = Pin.Size.values()[newSizeIndex]
                notifyItemChanged(adapterPosition)
            } else {
                // TODO: go to correct screen here
            }
        }

        override fun onLongClick(view: View?): Boolean {
            if(inChangeSizeActionMode)
                return true

            changeSizeActionMode = activity.startSupportActionMode(object : ActionMode.Callback {

                var saveNewSize = false

                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    inChangeSizeActionMode = true
                    changeSizeActionModeSubjectIndex = adapterPosition
                    prevSize = currentPin.size

                    activity.menuInflater.inflate(R.menu.menu_pin_action_mode, menu)

                    setNonSubjectViewAlpha(itemView, 0.5f)

                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return false
                }

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    saveNewSize = true
                    mode?.finish()
                    return true
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                    prevSize?.let {
                        val notifyChange = currentPin.size != it

                        if(!saveNewSize)
                            currentPin.size = it

                        if(notifyChange)
                            notifyItemChanged(adapterPosition)
                    }

                    setNonSubjectViewAlpha(itemView, 1f)

                    changeSizeActionMode = null
                    inChangeSizeActionMode = false
                    changeSizeActionModeSubjectIndex = -1
                }

            })

            return true
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

            pinText.setTextColor(if(tintNeeded) pin.getColorOnBackground(context) else Utils.getColor(context, R.color.colorText))
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

            // setting all text colors at once
            val textColor = if(tintNeeded) pin.getColorOnBackground(context) else Utils.getColor(context, R.color.colorText)

            lastActivityText.setTextColor(textColor)
            lastActivityTimeText.setTextColor(textColor)
            activityAreaText.setTextColor(textColor)
            deadlineText.setTextColor(textColor)
            goalNameText.setTextColor(textColor)
            goalPositionText.setTextColor(textColor)
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

    override fun onBindViewHolder(holder: PinViewHolder, position: Int) = holder.bind(pinStorage.pins[position])

    override fun getItemCount(): Int = pinStorage.pins.size

    override fun getItemViewType(position: Int): Int {
        return when(pinStorage.pins[position].size) {
            Pin.Size.SMALL -> VIEW_TYPE_SMALL
            Pin.Size.NORMAL -> VIEW_TYPE_NORMAL
            Pin.Size.LARGE -> VIEW_TYPE_LARGE
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        this.recyclerView = null
    }

    // this method sets the alpha of all views that are not the one that's currently the subject of an action mode
    // (this is a bit of a hacky solution but it should work)
    private fun setNonSubjectViewAlpha(subject: View, alpha: Float) {
        // reducing the opacity of all view holders that aren't selected
        recyclerView?.let {
            for(view in it.iterator()) {
                if(view == subject)
                    continue

                ObjectAnimator.ofFloat(view, "alpha", alpha).apply {
                    duration = Utils.getScaledAnimationDuration(context, 250)
                    start()
                }
            }
        }
    }



}