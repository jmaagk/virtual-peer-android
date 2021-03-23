package me.maagk.johannes.virtualpeer.chat

import me.maagk.johannes.virtualpeer.survey.question.Question

abstract class QuestionMessage(type: Int, message: String, val question: Question) : Message(type, message)