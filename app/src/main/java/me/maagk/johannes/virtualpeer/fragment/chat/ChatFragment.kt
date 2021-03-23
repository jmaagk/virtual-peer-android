package me.maagk.johannes.virtualpeer.fragment.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.fragment.FragmentActionBarTitle
import me.maagk.johannes.virtualpeer.chat.*
import me.maagk.johannes.virtualpeer.survey.question.*
import me.maagk.johannes.virtualpeer.view.ChoosePictureQuestionView
import me.maagk.johannes.virtualpeer.view.EmojiQuestionView
import me.maagk.johannes.virtualpeer.view.MultipleChoiceQuestionView
import me.maagk.johannes.virtualpeer.view.SliderQuestionView

class ChatFragment : Fragment(R.layout.fragment_chat), FragmentActionBarTitle {

    interface OnMessageSentListener {
        fun onMessageSent(message: Message)
    }

    companion object {
        const val TAG = "chat"
    }

    override val actionBarTitle: String
        get() = getString(R.string.nav_chat)

    private val messages = arrayListOf<Message>()
    private val messageQueue = arrayListOf<Message>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter

    private val onMessageSentListeners = arrayListOf<OnMessageSentListener>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.chatList)

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        val onQuestionClick = { holder: ChatAdapter.QuestionMessageViewHolder, clickedView: View ->
            val questionMessage = holder.currentMessage as QuestionMessage
            val question = questionMessage.question

            if(!question.answered) {
                question.answered = true

                when(holder) {
                    is ChatAdapter.EmojiQuestionMessageViewHolder -> {
                        val clickedTextView = clickedView as TextView
                        val clickedEmoji = clickedTextView.text.toString()

                        val emojiQuestion = question as EmojiQuestion
                        emojiQuestion.emojis.forEachIndexed start@ { index, emoji ->
                            if(emoji == clickedEmoji) {
                                emojiQuestion.answer = index
                                return@start
                            }
                        }

                        sendMessage(AnswerMessage(clickedEmoji, emojiQuestion))
                    }

                    is ChatAdapter.SliderQuestionMessageViewHolder -> {
                        val sliderQuestion = question as SliderQuestion
                        val sliderValue = sliderQuestion.answer as Float
                        val roundedValue = Utils.round(sliderValue, 1)
                        sliderQuestion.answer = roundedValue.toFloat()

                        sendMessage(AnswerMessage(roundedValue.toString(), sliderQuestion))
                    }

                    is ChatAdapter.MultipleChoiceQuestionMessageViewHolder -> {
                        val radioGroup = clickedView as RadioGroup
                        radioGroup.forEachIndexed start@ { index, view ->
                            if(view.id == radioGroup.checkedRadioButtonId) {
                                val radioButton = view as RadioButton
                                question.answer = index
                                sendMessage(AnswerMessage(radioButton.text.toString(), question))
                                return@start
                            }
                        }
                    }

                    is ChatAdapter.ChoosePictureQuestionMessageViewHolder -> {
                        val clickedImageButton = clickedView as ImageButton

                        var clickedIndex = -1
                        holder.choosePictureQuestionView.gridLayout.children.forEachIndexed start@ { index, view ->
                            val imageButton: ImageButton = view.findViewById(R.id.imageButton)
                            if(imageButton == clickedImageButton) {
                                clickedIndex = index
                                return@start
                            }
                        }

                        if(clickedIndex != -1) {
                            val choosePictureQuestion = (holder.currentMessage as ChoosePictureQuestionMessage).choosePictureQuestion
                            val label = choosePictureQuestion.images[clickedIndex].label
                            choosePictureQuestion.answer = clickedIndex
                            sendMessage(AnswerMessage(label, choosePictureQuestion))
                        }
                    }
                }
            }
        }

        adapter = ChatAdapter(messages, onQuestionClick)
        recyclerView.adapter = adapter

        val inputField: TextInputEditText = view.findViewById(R.id.chatInput)

        val sendButton: FloatingActionButton = view.findViewById(R.id.send)
        sendButton.setOnClickListener start@ {
            val userInput = inputField.text.toString()

            if(userInput.isEmpty())
                return@start

            val userMessage = prepareUserMessage(userInput)
            sendMessage(userMessage)
        }

        sendMessages(messageQueue.toTypedArray())
        messageQueue.clear()
    }

    private class ChatAdapter(val messages: ArrayList<Message>, val onQuestionClick: (QuestionMessageViewHolder, View) -> Unit) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

        private abstract class MessageViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

            val messageText: TextView = itemView.findViewById(R.id.message)
            var currentMessage: Message? = null

            open fun bind(message: Message) {
                currentMessage = message
                messageText.text = message.message
            }

        }

        private class IncomingMessageViewHolder(itemView: View) : MessageViewHolder(itemView)
        private class OutgoingMessageViewHolder(itemView: View) : MessageViewHolder(itemView)

        abstract class QuestionMessageViewHolder(itemView: View, val onClick: (QuestionMessageViewHolder, View) -> Unit) : MessageViewHolder(itemView)

        class EmojiQuestionMessageViewHolder(itemView: View, onClick: (QuestionMessageViewHolder, View) -> Unit) : QuestionMessageViewHolder(itemView, onClick) {

            val emojiQuestionView: EmojiQuestionView = itemView.findViewById(R.id.emojiQuestionView)

            init {
                emojiQuestionView.emoji1.setOnClickListener {
                    onClick(this, it)
                }
                emojiQuestionView.emoji2.setOnClickListener {
                    onClick(this, it)
                }
            }

            override fun bind(message: Message) {
                super.bind(message)

                val emojiQuestion = (message as EmojiQuestionMessage).emojiQuestion
                emojiQuestionView.question = emojiQuestion
            }

        }

        class SliderQuestionMessageViewHolder(itemView: View, onClick: (QuestionMessageViewHolder, View) -> Unit) : QuestionMessageViewHolder(itemView, onClick) {

            val sliderQuestionView: SliderQuestionView = itemView.findViewById(R.id.sliderQuestionView)
            val submitButton: Button = itemView.findViewById(R.id.submit)

            init {
                submitButton.setOnClickListener {
                    onClick(this, it)
                }

                sliderQuestionView.slider.addOnChangeListener { _, value, _ ->
                    (currentMessage as SliderQuestionMessage).sliderQuestion.answer = value
                }
            }

            override fun bind(message: Message) {
                super.bind(message)

                val sliderQuestion = (message as SliderQuestionMessage).sliderQuestion
                sliderQuestionView.question = sliderQuestion
            }

        }

        class MultipleChoiceQuestionMessageViewHolder(itemView: View, onClick: (QuestionMessageViewHolder, View) -> Unit) : QuestionMessageViewHolder(itemView, onClick) {

            val multipleChoiceQuestionView: MultipleChoiceQuestionView = itemView.findViewById(R.id.multipleChoiceQuestionView)

            init {
                multipleChoiceQuestionView.radioGroup.setOnCheckedChangeListener { group, id ->
                    onClick(this, group)
                }
            }

            override fun bind(message: Message) {
                super.bind(message)

                val multipleChoiceQuestion = (message as MultipleChoiceQuestionMessage).multipleChoiceQuestion
                multipleChoiceQuestionView.question = multipleChoiceQuestion

                // (re)selecting the option that was previously clicked
                if(multipleChoiceQuestion.answered) {
                    val selectedButtonIndex = multipleChoiceQuestion.answer as Int
                    multipleChoiceQuestionView.radioGroup.children.forEachIndexed start@ { index, view ->
                        if(index == selectedButtonIndex) {
                            multipleChoiceQuestionView.radioGroup.check(view.id)
                            return@start
                        }
                    }
                }
            }

        }

        class ChoosePictureQuestionMessageViewHolder(itemView: View, onClick: (QuestionMessageViewHolder, View) -> Unit) : QuestionMessageViewHolder(itemView, onClick) {

            val choosePictureQuestionView: ChoosePictureQuestionView = itemView.findViewById(R.id.choosePictureQuestionView)

            override fun bind(message: Message) {
                super.bind(message)

                val choosePictureQuestion = (message as ChoosePictureQuestionMessage).choosePictureQuestion
                choosePictureQuestionView.question = choosePictureQuestion

                choosePictureQuestionView.gridLayout.children.forEach { child ->
                    // TODO: make this more efficient
                    val imageButton: ImageButton = child.findViewById(R.id.imageButton)
                    imageButton.setOnClickListener {
                        onClick(this, it)
                    }
                }
            }

        }

        class TextInputQuestionMessageViewHolder(itemView: View) : QuestionMessageViewHolder(itemView, { _, _ -> })
        class AnswerMessageViewHolder(itemView: View) : QuestionMessageViewHolder(itemView, {_, _ -> })

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)

            return when(viewType) {
                Message.INCOMING -> {
                    val view = layoutInflater.inflate(R.layout.view_message_incoming, parent, false)
                    IncomingMessageViewHolder(view)
                }

                Message.EMOJI_QUESTION -> {
                    val view = layoutInflater.inflate(R.layout.view_message_question_emoji, parent, false)
                    EmojiQuestionMessageViewHolder(view, onQuestionClick)
                }

                Message.SLIDER_QUESTION -> {
                    val view = layoutInflater.inflate(R.layout.view_message_question_slider, parent, false)
                    SliderQuestionMessageViewHolder(view, onQuestionClick)
                }

                Message.MULTIPLE_CHOICE_QUESTION -> {
                    val view = layoutInflater.inflate(R.layout.view_message_question_multiple_choice, parent, false)
                    MultipleChoiceQuestionMessageViewHolder(view, onQuestionClick)
                }

                Message.CHOOSE_PICTURE_QUESTION -> {
                    val view = layoutInflater.inflate(R.layout.view_message_question_choose_picture, parent, false)
                    ChoosePictureQuestionMessageViewHolder(view, onQuestionClick)
                }

                Message.TEXT_INPUT_QUESTION -> {
                    val view = layoutInflater.inflate(R.layout.view_message_incoming, parent, false)
                    TextInputQuestionMessageViewHolder(view)
                }

                Message.ANSWER -> {
                    val view = layoutInflater.inflate(R.layout.view_message_outgoing, parent, false)
                    AnswerMessageViewHolder(view)
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

    private fun prepareUserMessage(input: String): Message {
        if(messages.size >= 1) {
            val prevMessage = messages[messages.size - 1]
            if(prevMessage is TextInputQuestionMessage) {
                prevMessage.question.answer = input
                prevMessage.question.answered = true
                return AnswerMessage(input, prevMessage.question)
            }
        }

        return Message(Message.OUTGOING, input)
    }

    fun sendMessages(toSend: Array<Message>) {
        val newMessages = toSend.size
        if(newMessages == 1) {
            sendMessage(toSend.first())
        } else {
            val prevMessageCount = messages.size
            messages.addAll(toSend)
            onSendMessages(toSend)
            adapter.notifyItemRangeInserted(prevMessageCount, newMessages)

            for(message in toSend)
                onMessageSent(message)
        }
    }

    fun sendMessage(toSend: Message) {
        messages.add(toSend)
        onSendMessage(toSend)
        adapter.notifyItemInserted(messages.size - 1)
        onMessageSent(toSend)
    }

    // these methods are separated from the ones above to isolate the listener-type behavior
    // from the actual sending of messages
    private fun onSendMessages(sent: Array<Message>) {
        for(message in sent)
            onSendMessage(message)
    }

    private fun onSendMessage(sent: Message) {
        // checking pre-defined "commands" (temporary)
        if(sent.type == Message.OUTGOING) {
            val botMessage = when(sent.message) {
                "emoji" -> {
                    val emojiQuestion = Question.getExampleEmojiQuestion(requireContext())
                    emojiQuestion.question = sent.message
                    EmojiQuestionMessage(emojiQuestion)
                }

                "slider", "slide" -> {
                    val sliderQuestion = Question.getExampleSliderQuestion(requireContext())
                    sliderQuestion.question = sent.message
                    SliderQuestionMessage(sliderQuestion)
                }

                "multiplechoice", "choice", "mc" -> {
                    val multipleChoiceQuestion = Question.getExampleMultipleChoiceQuestion(requireContext())
                    multipleChoiceQuestion.question = sent.message
                    MultipleChoiceQuestionMessage(multipleChoiceQuestion)
                }

                "picture", "image" -> {
                    val choosePictureQuestion = Question.getExampleChoosePictureQuestion(requireContext())
                    choosePictureQuestion.question = sent.message
                    ChoosePictureQuestionMessage(choosePictureQuestion)
                }

                "text", "textinput" -> {
                    val textInputQuestion = Question.getExampleTextInputQuestion(requireContext())
                    TextInputQuestionMessage(textInputQuestion)
                }

                else -> {
                    Message(Message.INCOMING, sent.message)
                }
            }

            sendMessage(botMessage)
        }

        recyclerView.smoothScrollToPosition(messages.size - 1)
    }

    fun queueMessage(message: Message) {
        if(this::recyclerView.isInitialized && this::adapter.isInitialized)
            sendMessage(message)
        else
            messageQueue.add(message)
    }

    private fun onMessageSent(message: Message) {
        for(listener in onMessageSentListeners)
            listener.onMessageSent(message)
    }

    fun addOnMessageSentListener(onMessageSentListener: OnMessageSentListener) {
        // TODO: this is not the ideal solution to this problem
        if(onMessageSentListeners.contains(onMessageSentListener))
            return

        onMessageSentListeners.add(onMessageSentListener)
    }

    fun removeOnMessageSentListener(onMessageSentListener: OnMessageSentListener) {
        onMessageSentListeners.remove(onMessageSentListener)
    }

}