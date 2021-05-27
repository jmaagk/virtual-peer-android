package me.maagk.johannes.virtualpeer.survey

import android.content.Context
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.survey.question.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class SurveyStorage(private val context: Context) {

    val VERSION = 1

    init {
        refresh()
    }

    lateinit var survey: Survey

    /**
     * Loads a survey from a given XML file
     */
    fun refresh() {
        // TODO: this is temporary; survey files should be fetched from the server
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
                        var sliderMin = -1f
                        var sliderMax = -1f
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
                                    sliderMin = questionChild.attributes.getNamedItem("min").nodeValue.toFloat()
                                    sliderMax = questionChild.attributes.getNamedItem("max").nodeValue.toFloat()
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

                                        val label = imageTag.attributes.getNamedItem("label").nodeValue

                                        val image = ChoosePictureQuestion.Image(drawableId, label)
                                        images.add(image)
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

        val fileVersion = surveyTag.getAttribute("version").toIntOrNull() ?: -1
        if(fileVersion >= 0 && fileVersion != VERSION)
            update(fileVersion)

        survey = Survey(title, description, questions)
    }

    /**
     * Saves the current survey's results to a file
     */
    fun save() {
        if(!this::survey.isInitialized)
            return

        save(survey)
    }

    /**
     * Saves a survey's results to a file
     */
    fun save(survey: Survey) {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        doc.xmlStandalone = true

        val root = doc.createElement("surveyResults")
        root.setAttribute("version", VERSION.toString())

        val titleTag = doc.createElement("title")
        titleTag.textContent = survey.title
        root.appendChild(titleTag)

        val questionsTag = doc.createElement("questions")

        for(question in survey.questions)
            questionsTag.appendChild(convertQuestionToXml(question, doc))

        root.appendChild(questionsTag)
        doc.appendChild(root)

        val transformer = TransformerFactory.newInstance().newTransformer()
        // some options to make the resulting files more readable
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        val input = DOMSource(doc)
        val output = StreamResult(File(context.filesDir, "survey_results.xml"))

        transformer.transform(input, output)
    }

    private fun convertQuestionToXml(question: Question, doc: Document): Element {
        var tagName = question::class.simpleName
        tagName = if(tagName == null) "question" else tagName[0].toLowerCase() + tagName.substring(1)

        val questionRoot = doc.createElement(tagName)

        val questionTag = doc.createElement("question")
        questionTag.textContent = question.question
        questionRoot.appendChild(questionTag)

        if(question.answered) {
            val answerTag = doc.createElement("answer")
            answerTag.textContent = question.answer.toString()
            questionRoot.appendChild(answerTag)
        }

        return questionRoot
    }

    private fun update(fromVersion: Int) {
        var updatedVersion = fromVersion

        // no update code just yet

        if(updatedVersion == VERSION)
            save() // this will make updates persistent
        else
            TODO("Add error handling for failed updates")
    }

}