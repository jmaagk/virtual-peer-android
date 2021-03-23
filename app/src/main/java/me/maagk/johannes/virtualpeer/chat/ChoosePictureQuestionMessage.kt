package me.maagk.johannes.virtualpeer.chat

import me.maagk.johannes.virtualpeer.survey.question.ChoosePictureQuestion

class ChoosePictureQuestionMessage(val choosePictureQuestion: ChoosePictureQuestion) : QuestionMessage(CHOOSE_PICTURE_QUESTION, choosePictureQuestion.question, choosePictureQuestion)