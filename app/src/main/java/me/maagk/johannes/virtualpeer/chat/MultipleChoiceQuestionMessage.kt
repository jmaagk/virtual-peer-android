package me.maagk.johannes.virtualpeer.chat

import me.maagk.johannes.virtualpeer.survey.question.MultipleChoiceQuestion

class MultipleChoiceQuestionMessage(val multipleChoiceQuestion: MultipleChoiceQuestion) : QuestionMessage(MULTIPLE_CHOICE_QUESTION, multipleChoiceQuestion.question, multipleChoiceQuestion)