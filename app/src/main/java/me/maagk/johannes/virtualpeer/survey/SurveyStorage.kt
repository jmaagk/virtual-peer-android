package me.maagk.johannes.virtualpeer.survey

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.survey.question.*
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

class SurveyStorage(private val context: Context) {

    init {
        refresh()
    }

    lateinit var survey: Survey

    fun refresh() {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(context.resources.openRawResource(R.raw.survey))

        var title = ""
        var description = ""
        var questions = arrayListOf<Question>()

        val surveyTag = doc.documentElement
        val surveyChildren = surveyTag.childNodes
        for(i in 0 until surveyChildren.length) {
            val surveyChild = surveyChildren.item(i)

            when(surveyChild.nodeName) {
                "title" -> {
                    title = surveyChild.textContent
                }

                "description" -> {
                    description = surveyChild.textContent
                }

                "questions" -> {
                    val questionsChildren = surveyChild.childNodes
                    for(j in 0 until questionsChildren.length) {
                        val questionsChild = questionsChildren.item(j)

                        if(questionsChild.nodeType != Element.ELEMENT_NODE)
                            continue

                        val questionTagName = questionsChild.nodeName
                        val questionChildren = questionsChild.childNodes

                        // working with null values here to make this more efficient
                        // (the variables will only receive a value when they're actually read)
                        var questionString: String? = null
                        var sliderMin = -1
                        var sliderMax = -1
                        var emojis: ArrayList<String>? = null
                        var choices: ArrayList<String>? = null
                        var images: ArrayList<ChoosePictureQuestion.Image>? = null

                        for(k in 0 until questionChildren.length) {
                            val questionChild = questionChildren.item(k)

                            if(questionChild.nodeType != Element.ELEMENT_NODE)
                                continue

                            when(questionChild.nodeName) {
                                "question" -> {
                                    questionString = questionChild.textContent
                                }

                                "slider" -> {
                                    sliderMin = questionChild.attributes.getNamedItem("min").nodeValue.toInt()
                                    sliderMax = questionChild.attributes.getNamedItem("max").nodeValue.toInt()
                                }

                                "emojis" -> {
                                    emojis = arrayListOf()
                                    for(l in 0 until questionChild.childNodes.length) {
                                        val emojiTag = questionChild.childNodes.item(l)

                                        if(emojiTag.nodeType != Element.ELEMENT_NODE)
                                            continue

                                        val emoji = emojiTag.textContent
                                        emojis.add(emoji)
                                    }
                                }

                                "choices" -> {
                                    choices = arrayListOf()
                                    for(l in 0 until questionChild.childNodes.length) {
                                        val choiceTag = questionChild.childNodes.item(l)

                                        if(choiceTag.nodeType != Element.ELEMENT_NODE)
                                            continue

                                        val choice = choiceTag.textContent
                                        choices.add(choice)
                                    }
                                }

                                "images" -> {
                                    images = arrayListOf()
                                    for(l in 0 until questionChild.childNodes.length) {
                                        val imageTag = questionChild.childNodes.item(l)

                                        if(imageTag.nodeType != Element.ELEMENT_NODE)
                                            continue

                                        val drawableName = imageTag.attributes.getNamedItem("src").nodeValue
                                        val drawableId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                                        val drawable = ResourcesCompat.getDrawable(context.resources, drawableId, context.theme)

                                        if(drawable != null) {
                                            val image = ChoosePictureQuestion.Image(drawable)
                                            images.add(image)
                                        }
                                    }
                                }
                            }
                        }

                        if(questionString == null) {
                            TODO("question can't be null")
                        }

                        var question: Question? = null
                        when(questionTagName) {
                            "textInputQuestion" -> question = TextInputQuestion(questionString)
                            "sliderQuestion" -> question = SliderQuestion(questionString, sliderMin, sliderMax)
                            "emojiQuestion" -> question = EmojiQuestion(questionString, emojis!!)
                            "multipleChoiceQuestion" -> question = MultipleChoiceQuestion(questionString, choices!!)
                            "choosePictureQuestion" -> question = ChoosePictureQuestion(questionString, images!!)
                        }

                        if(question != null)
                            questions.add(question)
                    }
                }
            }
        }

        survey = Survey(title, description, questions)
    }

}