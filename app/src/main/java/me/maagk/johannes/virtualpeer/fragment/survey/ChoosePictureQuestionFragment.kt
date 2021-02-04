package me.maagk.johannes.virtualpeer.fragment.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.misc.CustomGridLayoutManager
import me.maagk.johannes.virtualpeer.survey.question.ChoosePictureQuestion

class ChoosePictureQuestionFragment(question: ChoosePictureQuestion) : QuestionFragment(R.layout.fragment_question_choose_picture, question) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        var spanCount = 2
        val layoutManager = CustomGridLayoutManager(requireContext(), spanCount)
        recyclerView.layoutManager = layoutManager

        val choosePictureQuestion = question as ChoosePictureQuestion

        val adapter = ImageAdapter(choosePictureQuestion.images)
        recyclerView.adapter = adapter

        // adding a listener so the following code will run once the view is actually laid out and visible
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            // the following code disables scrolling in case it is not needed
            // (mainly to remove the overflow bumps that appear even when there is nothing to scroll through)
            val first = layoutManager.findFirstCompletelyVisibleItemPosition()
            val last = layoutManager.findLastCompletelyVisibleItemPosition()

            if(first == 0 && last == adapter.itemCount - 1)
                layoutManager.scrollingEnabled = false
        }
    }

    private class ImageAdapter(val images: ArrayList<ChoosePictureQuestion.Image>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

        private class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val imageButton: ImageButton = itemView.findViewById(R.id.imageButton)
            private var currentImage: ChoosePictureQuestion.Image? = null

            fun bind(image: ChoosePictureQuestion.Image) {
                currentImage = image

                imageButton.setImageDrawable(image.drawable)
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.single_image_item, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val image = images[position]

            holder.bind(image)
        }

        override fun getItemCount(): Int {
            return images.size
        }

    }

}