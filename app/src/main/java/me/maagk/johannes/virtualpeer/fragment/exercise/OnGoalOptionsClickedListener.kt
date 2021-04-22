package me.maagk.johannes.virtualpeer.fragment.exercise

interface OnGoalOptionsClickedListener {

    fun onDeleteClicked(matrixPart: EisenhowerMatrixFragment.MatrixPart, viewHolder: EisenhowerMatrixFragment.MatrixPart.GoalViewHolder)

    fun onPinClicked(matrixPart: EisenhowerMatrixFragment.MatrixPart, viewHolder: EisenhowerMatrixFragment.MatrixPart.GoalViewHolder)

}