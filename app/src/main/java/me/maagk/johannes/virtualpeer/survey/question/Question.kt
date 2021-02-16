package me.maagk.johannes.virtualpeer.survey.question

abstract class Question(var question: String) {

    abstract var answer: Any?
    var answered = false

}