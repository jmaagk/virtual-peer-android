package me.maagk.johannes.virtualpeer.chat

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