package me.maagk.johannes.virtualpeer.chat

import me.maagk.johannes.virtualpeer.survey.question.SliderQuestion

class SliderQuestionMessage(val sliderQuestion: SliderQuestion) : QuestionMessage(SLIDER_QUESTION, sliderQuestion.question, sliderQuestion)