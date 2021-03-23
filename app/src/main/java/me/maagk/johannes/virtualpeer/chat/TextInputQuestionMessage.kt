package me.maagk.johannes.virtualpeer.chat

import me.maagk.johannes.virtualpeer.survey.question.TextInputQuestion

class TextInputQuestionMessage(val textInputQuestion: TextInputQuestion) : QuestionMessage(TEXT_INPUT_QUESTION, textInputQuestion.question, textInputQuestion)