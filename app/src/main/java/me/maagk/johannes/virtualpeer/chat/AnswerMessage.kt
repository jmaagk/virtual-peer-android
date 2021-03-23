package me.maagk.johannes.virtualpeer.chat

import me.maagk.johannes.virtualpeer.survey.question.Question

class AnswerMessage(message: String?, val answeredQuestion: Question) : QuestionMessage(ANSWER, message ?: answeredQuestion.question, answeredQuestion)