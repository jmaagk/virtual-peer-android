package me.maagk.johannes.virtualpeer.survey.question

class EmojiQuestion(question: String, val emojis: ArrayList<String>) : Question(question) {

    // this will probably only ever have two values but a boolean is a bit confusing
    override var answer: Any? = -1

}