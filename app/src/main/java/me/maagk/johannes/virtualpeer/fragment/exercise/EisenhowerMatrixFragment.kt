package me.maagk.johannes.virtualpeer.fragment.exercise

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.exercise.EisenhowerMatrix

class EisenhowerMatrixFragment : Fragment(R.layout.fragment_eisenhower_matrix) {

    companion object {
        const val TAG = "eisenhowerMatrix"
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
    }

}