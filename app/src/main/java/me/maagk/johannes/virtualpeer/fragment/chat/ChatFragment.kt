package me.maagk.johannes.virtualpeer.fragment.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.fragment.FragmentActionBarTitle
import me.maagk.johannes.virtualpeer.survey.question.EmojiQuestion
import me.maagk.johannes.virtualpeer.survey.question.Question

class ChatFragment : Fragment(R.layout.fragment_chat), FragmentActionBarTitle {

    override val actionBarTitle: String
        get() = getString(R.string.nav_drawer_chat)

    private val messages = arrayListOf<Message>()

    open class Message(val type: Int, val message: String) {

        companion object {
            const val INCOMING = 0
            const val OUTGOING = 1
            const val EMOJI_QUESTION = 2
        }

    }

    abstract class QuestionMessage(type: Int, message: String, val question: Question) : Message(type, message)
    class EmojiQuestionMessage(message: String, val emojiQuestion: EmojiQuestion) : QuestionMessage(EMOJI_QUESTION, message, emojiQuestion)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.chatList)

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        val adapter = ChatAdapter(messages)
        recyclerView.adapter = adapter

        val inputField: TextInputEditText = view.findViewById(R.id.chatInput)

        val sendButton: FloatingActionButton = view.findViewById(R.id.send)
        sendButton.setOnClickListener {
            val userInput = inputField.text.toString()
            messages.add(Message(Message.OUTGOING, userInput))

            when(userInput) {
                "emoji" -> {
                    val emojis = arrayListOf("\uD83D\uDC4E", "\uD83D\uDC4D")
                    val emojiQuestion = EmojiQuestion("", emojis)
                    messages.add(EmojiQuestionMessage(userInput, emojiQuestion))
                }

                else -> {
                    messages.add(Message(Message.INCOMING, inputField.text.toString()))
                }
            }

            adapter.notifyDataSetChanged()
        }
    }

    private class ChatAdapter(val messages: ArrayList<Message>) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

        private abstract class MessageViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

            protected val messageText: TextView = itemView.findViewById(R.id.message)
            protected var currentMessage: Message? = null

            open fun bind(message: Message) {
                messageText.text = message.message
            }

        }

        private class IncomingMessageViewHolder(itemView: View) : MessageViewHolder(itemView)
        private class OutgoingMessageViewHolder(itemView: View) : MessageViewHolder(itemView)
        private class EmojiQuestionMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {

            val emoji1: TextView = itemView.findViewById(R.id.emoji1)
            val emoji2: TextView = itemView.findViewById(R.id.emoji2)

            override fun bind(message: Message) {
                super.bind(message)

                val emojiQuestion = (message as EmojiQuestionMessage).emojiQuestion
                emojiQuestion.emojis.forEachIndexed{index, emoji ->
                    when(index) {
                        0 -> emoji1.text = emoji
                        1 -> emoji2.text = emoji
                        else -> TODO("do something when more emojis are supplied")
                    }
                }
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)

            return when(viewType) {
                Message.INCOMING -> {
                    val view = layoutInflater.inflate(R.layout.view_message_incoming, parent, false)
                    IncomingMessageViewHolder(view)
                }

                Message.EMOJI_QUESTION -> {
                    val view = layoutInflater.inflate(R.layout.view_message_question_emoji, parent, false)
                    EmojiQuestionMessageViewHolder(view)
                }

                else -> {
                    val view = layoutInflater.inflate(R.layout.view_message_outgoing, parent, false)
                    OutgoingMessageViewHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val message = messages[position]
            holder.bind(message)
        }

        override fun getItemCount(): Int {
            return messages.size
        }

        override fun getItemViewType(position: Int): Int {
            return messages[position].type
        }

    }

}