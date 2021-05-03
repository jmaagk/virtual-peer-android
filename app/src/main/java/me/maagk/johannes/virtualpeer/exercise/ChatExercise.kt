package me.maagk.johannes.virtualpeer.exercise

import me.maagk.johannes.virtualpeer.UserProfile
import me.maagk.johannes.virtualpeer.chat.AnswerMessage
import me.maagk.johannes.virtualpeer.chat.Message
import me.maagk.johannes.virtualpeer.chat.MultipleChoiceQuestionMessage
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment
import me.maagk.johannes.virtualpeer.survey.question.MultipleChoiceQuestion

abstract class ChatExercise(protected val chatFragment: ChatFragment) : ChatFragment.OnMessageSentListener {

    private lateinit var startQuestion: MultipleChoiceQuestion
    private lateinit var startMessage: MultipleChoiceQuestionMessage

    private lateinit var moreInfoQuestion: MultipleChoiceQuestion
    private lateinit var moreInfoMessage: MultipleChoiceQuestionMessage

    protected lateinit var rateQuestion: MultipleChoiceQuestion
    private lateinit var rateMessage: MultipleChoiceQuestionMessage

    protected val context = chatFragment.requireContext()
    protected val userProfile = UserProfile(context)

    init {
        chatFragment.addOnMessageSentListener(this)
    }

    fun prepare() {
        startQuestion = MultipleChoiceQuestion("", ArrayList())
        startQuestion = createStartQuestion(startQuestion)

        startMessage = MultipleChoiceQuestionMessage(startQuestion)

        sendMessage(startMessage)
    }

    abstract fun start()

    abstract fun createStartQuestion(startQuestion: MultipleChoiceQuestion): MultipleChoiceQuestion

    abstract fun createMoreInfoQuestion(moreInfoQuestion: MultipleChoiceQuestion): MultipleChoiceQuestion

    abstract fun createRateQuestion(ratingQuestion: MultipleChoiceQuestion): MultipleChoiceQuestion

    override fun onMessageSent(message: Message) {
        if(message is AnswerMessage) {
            val answeredQuestion = message.answeredQuestion

            if(::startQuestion.isInitialized && answeredQuestion == startQuestion && startQuestion.answered) {
                when(startQuestion.answer as Int) {
                    0 -> {
                        // sending info on the exercise
                        moreInfoQuestion = MultipleChoiceQuestion("", ArrayList())
                        moreInfoQuestion = createMoreInfoQuestion(moreInfoQuestion)

                        moreInfoMessage = MultipleChoiceQuestionMessage(moreInfoQuestion)
                        sendMessage(moreInfoMessage)
                    }

                    1 -> start()
                }
            }

            if(::moreInfoQuestion.isInitialized && answeredQuestion == moreInfoQuestion && moreInfoQuestion.answered) {
                start()
            }

            if(::rateQuestion.isInitialized && answeredQuestion == rateQuestion && rateQuestion.answered) {
                onRateQuestionAnswered()
            }
        }
    }

    // this is a bit of a hack as the initialization state of lateinit vars cannot be checked in subclasses
    abstract fun onRateQuestionAnswered()

    protected fun sendMessage(message: Message) {
        chatFragment.sendMessage(message)
    }

    protected fun queueMessage(message: Message) {
        chatFragment.queueMessage(message)
    }

    fun rate() {
        rateQuestion = MultipleChoiceQuestion("", ArrayList())
        rateQuestion = createRateQuestion(rateQuestion)

        rateMessage = MultipleChoiceQuestionMessage(rateQuestion)

        // queueing this message because the fragment may not be fully initialized yet at this point
        queueMessage(rateMessage)
    }

}