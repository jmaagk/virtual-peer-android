package me.maagk.johannes.virtualpeer.survey

import me.maagk.johannes.virtualpeer.survey.question.Question
import java.io.Serializable

class Survey(val title: String, val description: String, val questions: ArrayList<Question>) : Serializable