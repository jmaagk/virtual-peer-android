package me.maagk.johannes.virtualpeer.fragment.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.exercise.AddGoalDialog
import me.maagk.johannes.virtualpeer.exercise.EisenhowerMatrix
import me.maagk.johannes.virtualpeer.goals.Goal

class EisenhowerMatrixFragment : Fragment(R.layout.fragment_eisenhower_matrix), AddGoalDialog.OnGoalCompletedListener {

    companion object {
        const val TAG = "eisenhowerMatrix"
    }

    private lateinit var urgentImportantPart: MatrixPart
    private lateinit var notUrgentImportantPart: MatrixPart
    private lateinit var urgentNotImportantPart: MatrixPart
    private lateinit var notUrgentNotImportantPart: MatrixPart

    private inner class MatrixPart(val rootLayout: LinearLayout, val position: EisenhowerMatrix.Position) {

        val goals = ArrayList<Goal>()

        val titleText: TextView = rootLayout.findViewById(R.id.eisenhowerMatrixPositionTitle)
        val goalListCard: CardView = rootLayout.findViewById(R.id.goalListCard)
        val goalList: RecyclerView = goalListCard.findViewById(R.id.goalList)

        init {
            // setting the background color of this part
            rootLayout.setBackgroundColor(position.getColor(requireContext()))

            // adjusting the position of this part's list
            // 2 of these have the majority of their padding on the left, the others on the right
            val marginLarge = Utils.dpToPx(35f, requireContext().resources.displayMetrics).toInt()
            val marginSmall = Utils.dpToPx(2.5f, requireContext().resources.displayMetrics).toInt()

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

            titleText.text = position.getTitle(requireContext())

            val layoutManager = LinearLayoutManager(context)
            goalList.layoutManager = layoutManager

            goalList.adapter = GoalListAdapter()
        }

        inner class GoalListAdapter() : RecyclerView.Adapter<GoalViewHolder>() {

            private lateinit var layoutInflater: LayoutInflater

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
                if(!::layoutInflater.isInitialized)
                    layoutInflater = LayoutInflater.from(parent.context)

                val view = layoutInflater.inflate(R.layout.view_eisenhower_matrix_goal, parent, false)
                return GoalViewHolder(view)
            }

            override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
                holder.bind(goals[position])
            }

            override fun getItemCount(): Int {
                return goals.size
            }

        }

        inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val goalCard: CardView = itemView.findViewById(R.id.goalCard)
            val goalName: TextView = goalCard.findViewById(R.id.goalName)
            val goalInfo: TextView = goalCard.findViewById(R.id.goalInfo)
            val goalCheckBox: CheckBox = goalCard.findViewById(R.id.goalCheckBox)

            init {
                goalCard.setOnClickListener {
                    goalCheckBox.isChecked = !goalCheckBox.isChecked
                }
            }

            fun bind(goal: Goal) {
                goalName.text = goal.name
                goalInfo.text = "Placeholder"
                // TODO: set info on deadline / something else
                // TODO: set onCheckedChangeListener for check box
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        urgentImportantPart = MatrixPart(view.findViewById(R.id.urgentImportantLayout), EisenhowerMatrix.Position.URGENT_IMPORTANT)
        notUrgentImportantPart = MatrixPart(view.findViewById(R.id.notUrgentImportantLayout), EisenhowerMatrix.Position.NOT_URGENT_IMPORTANT)
        urgentNotImportantPart = MatrixPart(view.findViewById(R.id.urgentNotImportantLayout), EisenhowerMatrix.Position.URGENT_NOT_IMPORTANT)
        notUrgentNotImportantPart = MatrixPart(view.findViewById(R.id.notUrgentNotImportantLayout), EisenhowerMatrix.Position.NOT_URGENT_NOT_IMPORTANT)

        val addGoalButton: FloatingActionButton = view.findViewById(R.id.addGoal)
        addGoalButton.setOnClickListener {
            val dialog = AddGoalDialog(requireContext())
            dialog.onGoalCompletedListener = this
            dialog.show()
        }
    }

    override fun onGoalCompleted(goal: Goal) {
        val part = when(goal.position) {
            EisenhowerMatrix.Position.URGENT_IMPORTANT -> urgentImportantPart
            EisenhowerMatrix.Position.NOT_URGENT_IMPORTANT -> notUrgentImportantPart
            EisenhowerMatrix.Position.URGENT_NOT_IMPORTANT -> urgentNotImportantPart
            EisenhowerMatrix.Position.NOT_URGENT_NOT_IMPORTANT -> notUrgentNotImportantPart
        }

        part.goals.add(goal)

        val adapter = part.goalList.adapter
        adapter?.let {
            adapter.notifyItemInserted(adapter.itemCount)
        }
    }

}