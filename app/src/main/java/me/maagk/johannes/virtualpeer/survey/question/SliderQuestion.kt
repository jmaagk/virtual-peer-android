package me.maagk.johannes.virtualpeer.survey.question

class SliderQuestion(question: String, var min: Float, var max: Float) : Question(question) {

    override var answer: Any? = 0f

}