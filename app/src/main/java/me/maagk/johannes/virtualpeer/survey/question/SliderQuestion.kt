package me.maagk.johannes.virtualpeer.survey.question

class SliderQuestion(question: String, var min: Int, var max: Int) : Question(question) {

    override var answer: Any? = 0f

}