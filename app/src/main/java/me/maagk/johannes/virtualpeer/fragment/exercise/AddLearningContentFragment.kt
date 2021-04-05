package me.maagk.johannes.virtualpeer.fragment.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.exercise.LearningContent

class AddLearningContentFragment : Fragment(R.layout.fragment_add_learning_content) {

    interface OnLearningContentsFinishedListener {
        fun onLearningContentsFinished(learningContents: ArrayList<LearningContent>)
    }

    companion object {
        const val TAG = "addLearningContent"
    }

    val learningContents = arrayListOf<LearningContent>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var noContentText: TextView
    private lateinit var finishButton: FloatingActionButton

    lateinit var onLearningContentsFinishedListener: OnLearningContentsFinishedListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addButton: FloatingActionButton = view.findViewById(R.id.addLearningContent)
        addButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_learning_content, null)
            val contentInput: TextInputEditText = dialogView.findViewById(R.id.learningContentInput)

            AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton(R.string.pomodoro_learning_content_dialog_add) start@ { _, _ ->
                        if(contentInput.text == null || contentInput.text.isNullOrBlank())
                            return@start

                        val learningContent = LearningContent(contentInput.text.toString(), 25)
                        addLearningContent(learningContent)
                    }
                    .setNegativeButton(R.string.pomodoro_learning_content_dialog_cancel, null)
                    .show()
        }

        recyclerView = view.findViewById(R.id.learningContentList)

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        val adapter = LearningContentAdapter()
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            // forwarding all calls to this method to just write this once
            override fun onChanged() {
                // updating the visibility of the no content text (info on where content will be shown)
                noContentText.visibility = if(learningContents.size == 0) View.VISIBLE else View.GONE

                if(learningContents.size == 0 && finishButton.visibility == View.VISIBLE) {
                    finishButton.isEnabled = false
                    val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down)
                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}

                        override fun onAnimationEnd(animation: Animation?) {
                            finishButton.visibility = View.GONE
                        }

                        override fun onAnimationRepeat(animation: Animation?) {}
                    })
                    finishButton.startAnimation(animation)
                } else if(learningContents.size > 0 && finishButton.visibility == View.GONE) {
                    finishButton.isEnabled = true
                    val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
                    finishButton.startAnimation(animation)
                    finishButton.visibility = View.VISIBLE
                }
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                onChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                onChanged()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                onChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                onChanged()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                onChanged()
            }
        })
        recyclerView.adapter = adapter

        noContentText = view.findViewById(R.id.noContentText)

        finishButton = view.findViewById(R.id.finish)
        finishButton.setOnClickListener {
            // TODO: is this safe?
            parentFragmentManager.popBackStack()

            if(::onLearningContentsFinishedListener.isInitialized)
                onLearningContentsFinishedListener.onLearningContentsFinished(learningContents)
        }
    }

    private fun addLearningContent(learningContent: LearningContent) {
        var breakAdded = false

        if(learningContents.size != 0) {
            val prevContent = learningContents[learningContents.size - 1]

            // adding a break
            if(prevContent !is LearningContent.Break) {
                var breakDuration = 5

                var breakCount = 0
                for(content in learningContents)
                    if(content is LearningContent.Break)
                        breakCount++

                // every third break will be longer
                if((breakCount + 1) % 3 == 0)
                    breakDuration = 30

                learningContents.add(LearningContent.Break(breakDuration))

                breakAdded = true
            }
        }

        learningContents.add(learningContent)

        if(breakAdded)
            recyclerView.adapter?.notifyItemRangeInserted(learningContents.size - 1, 2)
        else
            recyclerView.adapter?.notifyItemInserted(learningContents.size)
    }

    private class LearningContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val durationText: TextView = view.findViewById(R.id.durationText)
        val contentText: TextView = view.findViewById(R.id.contentText)

        fun bind(learningContent: LearningContent) {
            durationText.text = itemView.context.getString(R.string.pomodoro_learning_content_duration, learningContent.durationMinutes)

            if(learningContent is LearningContent.Break)
                contentText.text = itemView.context.getString(R.string.pomodoro_learning_content_break)
            else
                contentText.text = learningContent.content
        }

    }

    private inner class LearningContentAdapter() : RecyclerView.Adapter<LearningContentViewHolder>() {

        private lateinit var layoutInflater: LayoutInflater

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LearningContentViewHolder {
            if(!::layoutInflater.isInitialized)
                layoutInflater = LayoutInflater.from(parent.context)

            val view = layoutInflater.inflate(R.layout.view_learning_content, parent, false)
            return LearningContentViewHolder(view)
        }

        override fun onBindViewHolder(holder: LearningContentViewHolder, position: Int) {
            holder.bind(this@AddLearningContentFragment.learningContents[position])
        }

        override fun getItemCount(): Int {
            return this@AddLearningContentFragment.learningContents.size
        }

    }

}