package me.maagk.johannes.virtualpeer.fragment.library

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.exercise.BoxBreathingExercise
import me.maagk.johannes.virtualpeer.exercise.Exercise
import me.maagk.johannes.virtualpeer.exercise.MeditationExercise
import me.maagk.johannes.virtualpeer.exercise.PomodoroExercise

class LibraryFragment : Fragment(R.layout.fragment_library) {

    companion object {
        const val TAG = "library"
    }

    private val exercises = arrayListOf<Exercise>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // creating the list of exercises
        if(exercises.isEmpty()) {
            exercises.add(PomodoroExercise(requireContext()))
            exercises.add(BoxBreathingExercise(requireContext()))
            exercises.add(MeditationExercise(requireContext()))
        }

        val viewPager: ViewPager2 = view.findViewById(R.id.exerciseViewPager)
        val adapter = ExerciseAdapter()
        viewPager.adapter = adapter
        viewPager.apply {
            val recyclerView = getChildAt(0) as RecyclerView
            recyclerView.apply {
                val padding = Utils.dpToPx(65f, requireContext().resources.displayMetrics).toInt()
                setPadding(padding, 0, padding, 0)
                clipToPadding = false
            }
        }
    }

    private inner class ExerciseAdapter() : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

        inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val exerciseCard: CardView = itemView.findViewById(R.id.exerciseCard)

            val exerciseTitleText: TextView = itemView.findViewById(R.id.exerciseTitleText)
            val pinIcon: ImageView = itemView.findViewById(R.id.pinIcon)

            val lastActivityText: TextView = itemView.findViewById(R.id.lastActivityText)
            val lastActivityTimeText: TextView = itemView.findViewById(R.id.lastActivityTimeText)

            val totalTimeText: TextView = itemView.findViewById(R.id.totalTimeText)
            val totalTimeTimeText: TextView = itemView.findViewById(R.id.totalTimeTimeText)

            val exerciseInfoButton: Button = itemView.findViewById(R.id.exerciseInfoButton)
            val exerciseStartButton: Button = itemView.findViewById(R.id.exerciseStartButton)

            lateinit var currentExercise: Exercise

            init {
                exerciseInfoButton.setOnClickListener {
                    val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(currentExercise.name)
                            .setMessage(currentExercise.info)
                            .setPositiveButton(R.string.library_exercise_info_dialog_okay, null)
                            .show()

                    val dialogMessage: TextView? = dialog.findViewById(android.R.id.message)
                    dialogMessage?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                }

                // TODO: start exercise here
            }

            fun bind(exercise: Exercise) {
                currentExercise = exercise

                exerciseCard.setCardBackgroundColor(exercise.color)

                exerciseTitleText.text = exercise.name
                exerciseTitleText.setTextColor(exercise.textColor)

                val states = Array(1) {
                    IntArray(1)
                }
                states[0][0] = android.R.attr.state_enabled

                val colors = IntArray(1)
                colors[0] = exercise.textColor
                pinIcon.imageTintList = ColorStateList(states, colors)

                lastActivityText.setTextColor(exercise.textColor)
                lastActivityTimeText.setTextColor(exercise.textColor)

                totalTimeText.setTextColor(exercise.textColor)
                totalTimeTimeText.setTextColor(exercise.textColor)

                exerciseInfoButton.setTextColor(exercise.buttonTextColor)
                exerciseInfoButton.setBackgroundResource(R.drawable.round_button)
                exerciseInfoButton.backgroundTintList = null

                exerciseStartButton.setTextColor(exercise.buttonTextColor)
                exerciseStartButton.setBackgroundResource(R.drawable.round_button)
                exerciseStartButton.backgroundTintList = null
            }

        }

        private lateinit var layoutInflater: LayoutInflater

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
            if(!::layoutInflater.isInitialized)
                layoutInflater = LayoutInflater.from(parent.context)

            val view = layoutInflater.inflate(R.layout.view_exercise, parent, false)
            return ExerciseViewHolder(view)
        }

        override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
            holder.bind(exercises[position])
        }

        override fun getItemCount(): Int {
            return exercises.size
        }

    }

}