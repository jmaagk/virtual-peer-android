package me.maagk.johannes.virtualpeer.fragment.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.fragment.FragmentActionBarTitle
import me.maagk.johannes.virtualpeer.survey.question.*
import me.maagk.johannes.virtualpeer.view.ChoosePictureQuestionView
import me.maagk.johannes.virtualpeer.view.EmojiQuestionView

class ChatFragment : Fragment(R.layout.fragment_chat), FragmentActionBarTitle {

    override val actionBarTitle: String
        get() = getString(R.string.nav_drawer_chat)

    private val messages = arrayListOf<Message>()
    private lateinit var adapter: ChatAdapter

    open class Message(var type: Int, val message: String) {

        companion object {
            const val INCOMING = 0
            const val OUTGOING = 1
            const val ANSWER = 2
            const val EMOJI_QUESTION = 3
            const val SLIDER_QUESTION = 4
            const val MULTIPLE_CHOICE_QUESTION = 5
            const val CHOOSE_PICTURE_QUESTION = 6
            const val TEXT_INPUT_QUESTION = 7
        }

    }

    abstract class QuestionMessage(type: Int, message: String, val question: Question) : Message(type, message)
    class EmojiQuestionMessage(message: String, val emojiQuestion: EmojiQuestion) : QuestionMessage(EMOJI_QUESTION, message, emojiQuestion)
    class SliderQuestionMessage(message: String, val sliderQuestion: SliderQuestion) : QuestionMessage(SLIDER_QUESTION, message, sliderQuestion)
    class MultipleChoiceQuestionMessage(message: String, val multipleChoiceQuestion: MultipleChoiceQuestion) : QuestionMessage(MULTIPLE_CHOICE_QUESTION, message, multipleChoiceQuestion)
    class ChoosePictureQuestionMessage(message: String, val choosePictureQuestion: ChoosePictureQuestion) : QuestionMessage(CHOOSE_PICTURE_QUESTION, message, choosePictureQuestion)
    class TextInputQuestionMessage(message: String, val textInputQuestion: TextInputQuestion) : QuestionMessage(TEXT_INPUT_QUESTION, message, textInputQuestion)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.chatList)

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        val onQuestionClick = { holder: ChatAdapter.QuestionMessageViewHolder, clickedView: View ->
            val questionMessage = holder.currentMessage as QuestionMessage
            val question = questionMessage.question

            if(!question.answered) {
                when(holder) {
                    is ChatAdapter.EmojiQuestionMessageViewHolder -> {
                        val clickedTextView = clickedView as TextView
                        val emoji = clickedTextView.text.toString()

                        sendMessage(Message(Message.ANSWER, emoji))
                    }

                    is ChatAdapter.SliderQuestionMessageViewHolder -> {
                        val sliderValue = (question as SliderQuestion).input
                        sendMessage(Message(Message.ANSWER, Utils.round(sliderValue, 1).toString()))
                    }

                    is ChatAdapter.MultipleChoiceQuestionMessageViewHolder -> {
                        val radioGroup = clickedView as RadioGroup
                        radioGroup.forEach start@ {
                            if(it.id == radioGroup.checkedRadioButtonId) {
                                val radioButton = it as RadioButton
                                sendMessage(Message(Message.ANSWER, radioButton.text.toString()))
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
                            sendMessage(Message(Message.ANSWER, label))
                        }
                    }
                }
            }

            question.answered = true
        }

        adapter = ChatAdapter(messages, onQuestionClick)
        recyclerView.adapter = adapter

        val inputField: TextInputEditText = view.findViewById(R.id.chatInput)

        val sendButton: FloatingActionButton = view.findViewById(R.id.send)
        sendButton.setOnClickListener {
            val userInput = inputField.text.toString()
            val userMessage = Message(Message.OUTGOING, userInput)
            sendMessage(userMessage)
        }
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

            val slider: Slider = itemView.findViewById(R.id.slider)
            val submitButton: Button = itemView.findViewById(R.id.submit)
            lateinit var sliderQuestion: SliderQuestion

            init {
                slider.addOnChangeListener { slider, value, fromUser ->
                    sliderQuestion.input = value
                }
                submitButton.setOnClickListener {
                    onClick(this, it)
                }
            }

            override fun bind(message: Message) {
                super.bind(message)

                sliderQuestion = (message as SliderQuestionMessage).sliderQuestion
                slider.value = sliderQuestion.input
            }

        }

        class MultipleChoiceQuestionMessageViewHolder(itemView: View, onClick: (QuestionMessageViewHolder, View) -> Unit) : QuestionMessageViewHolder(itemView, onClick) {

            val radioGroup: RadioGroup = itemView.findViewById(R.id.radioGroup)

            init {
                radioGroup.setOnCheckedChangeListener { group, id ->
                    onClick(this, group)
                }
            }

            override fun bind(message: Message) {
                super.bind(message)

                // TODO: improve this process by not inflating every time this binds to a message
                val multipleChoiceQuestion = (message as MultipleChoiceQuestionMessage).multipleChoiceQuestion
                radioGroup.removeAllViews()
                val inflater = LayoutInflater.from(radioGroup.context)
                for(choice in multipleChoiceQuestion.choices) {
                    val radioButton = inflater.inflate(R.layout.view_radio_button, radioGroup, false) as RadioButton
                    radioButton.text = choice
                    radioGroup.addView(radioButton)
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

    private fun sendMessages(toSend: Array<Message>) {
        val newMessages = toSend.size
        if(newMessages == 1) {
            sendMessage(toSend.first())
        } else {
            val prevMessageCount = messages.size
            messages.addAll(toSend)
            onSendMessages(toSend)
            adapter.notifyItemRangeInserted(prevMessageCount, newMessages)
        }
    }

    private fun sendMessage(toSend: Message) {
        messages.add(toSend)
        onSendMessage(toSend)
        adapter.notifyItemInserted(messages.size - 1)
    }

    // these methods are separated from the ones above to isolate the listener-type behavior
    // from the actual sending of messages
    private fun onSendMessages(sent: Array<Message>) {
        for(message in sent)
            onSendMessage(message)
    }

    private fun onSendMessage(sent: Message) {
        // checking if this message is an answer to a previously asked question
        if(sent !is QuestionMessage && messages.size >= 2) {
            val prevMessage = messages[messages.size - 2]
            if(prevMessage is TextInputQuestionMessage && !prevMessage.question.answered) {
                sent.type = Message.ANSWER
                prevMessage.question.answered = true
            }
        }

        // checking pre-defined "commands" (temporary)
        if(sent.type == Message.OUTGOING) {
            val botMessage = when(sent.message) {
                "emoji" -> {
                    val emojis = arrayListOf("\uD83D\uDC4E", "\uD83D\uDC4D")
                    val emojiQuestion = EmojiQuestion("", emojis)
                    EmojiQuestionMessage(sent.message, emojiQuestion)
                }

                "slider", "slide" -> {
                    SliderQuestionMessage(sent.message, SliderQuestion("", 0, 10))
                }

                "multiplechoice", "choice", "mc" -> {
                    val lorem = getString(R.string.lorem_ipsum_short)
                    val multipleChoiceQuestion = MultipleChoiceQuestion("", arrayListOf("$lorem 1", "$lorem 2", "$lorem 3"))
                    MultipleChoiceQuestionMessage(sent.message, multipleChoiceQuestion)
                }

                "picture", "image" -> {
                    val resources = requireContext().resources
                    val theme = requireContext().theme

                    val images = ArrayList<ChoosePictureQuestion.Image>()
                    ResourcesCompat.getDrawable(resources, R.drawable.test_image_1, theme)?.let { images.add(ChoosePictureQuestion.Image(it, "Lorem 1")) }
                    ResourcesCompat.getDrawable(resources, R.drawable.test_image_2, theme)?.let { images.add(ChoosePictureQuestion.Image(it, "Lorem 2")) }
                    ResourcesCompat.getDrawable(resources, R.drawable.test_image_3, theme)?.let { images.add(ChoosePictureQuestion.Image(it, "Lorem 3")) }
                    ResourcesCompat.getDrawable(resources, R.drawable.test_image_4, theme)?.let { images.add(ChoosePictureQuestion.Image(it, "Lorem 4")) }

                    val choosePictureQuestion = ChoosePictureQuestion("", images)
                    ChoosePictureQuestionMessage(sent.message, choosePictureQuestion)
                }

                "text", "textinput" -> {
                    val textInputQuestion = TextInputQuestion("")
                    TextInputQuestionMessage(getString(R.string.lorem_ipsum_medium), textInputQuestion)
                }

                else -> {
                    Message(Message.INCOMING, sent.message)
                }
            }

            sendMessage(botMessage)
        }
    }

}