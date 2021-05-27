package me.maagk.johannes.virtualpeer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.EmojiQuestion

class EmojiQuestionView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr), View.OnClickListener {

    var question: EmojiQuestion? = null
        set(value) {
            field = value
            update()
        }

    val emoji1: TextView
    val emoji2: TextView

    init {
        inflate(context, R.layout.view_question_emoji, this)

        emoji1 = findViewById(R.id.emoji1)
        emoji1.setOnClickListener(this)

        emoji2 = findViewById(R.id.emoji2)
        emoji2.setOnClickListener(this)
    }

    fun update() {
        question?.emojis?.forEachIndexed{index, emoji ->
            when(index) {
                0 -> emoji1.text = emoji
                1 -> emoji2.text = emoji
                else -> TODO("do something when more emojis are supplied")
            }
        }
    }

    override fun onClick(view: View?) {
        if(view !is TextView)
            return

        question?.emojis?.forEachIndexed { index, emoji ->
            if(emoji == view.text) {
                question?.answer = index
                question?.answered = true
                return@forEachIndexed
            }
        }
    }

}