package me.maagk.johannes.virtualpeer.survey.question

class MultipleChoiceQuestion(question: String, var choices: ArrayList<String>) : Question(question) {

    override var answer: Any? = -1

}