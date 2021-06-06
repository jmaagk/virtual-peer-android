package me.maagk.johannes.virtualpeer

import android.content.Context
import android.net.Uri
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

abstract class Storage<T>(protected val context: Context, refresh: Boolean = true) {

    companion object {
        fun Document.transformToString(): String {
            val writer = StringWriter()

            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            transformer.transform(DOMSource(this), StreamResult(writer))

            return writer.toString()
        }
    }

    abstract val FILE_NAME: String
    abstract val VERSION: Int

    val file: File
        get() = File(context.filesDir, FILE_NAME)

    val items = mutableListOf<T>()

    init {
        // refreshing the internal list of elements if the user of this class wants it to happen
        if(refresh)
            refresh()

        // making sure the "files" directory in the app's directory exists
        if(!context.filesDir.exists()) {
            val success = context.filesDir.mkdir()
            if(!success)
                TODO("Error handling: can't save when files directory doesn't exist and can't be created")
        }
    }

    open fun refresh() {
        if(!file.exists())
            return

        // getting the file as a document
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(context.contentResolver.openInputStream(Uri.fromFile(file)))

        // clearing all current items as this method is responsible for reading them
        items.clear()

        // updating the internal list of items
        refreshList(doc)

        val fileVersion = doc.documentElement.getAttribute("version").toInt()

        if(fileVersion >= 0 && fileVersion != VERSION)
            update(fileVersion)
    }

    protected abstract fun refreshList(doc: Document)

    fun getFinishedRootElement(doc: Document, itemsToInclude: List<T>): Element {
        val root = getRootElement(doc)
        root.setAttribute("version", VERSION.toString())

        for(item in itemsToInclude)
            root.appendChild(convertItemToXml(item, doc))

        return root
    }

    fun save() {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        doc.xmlStandalone = true

        doc.appendChild(getFinishedRootElement(doc, items))

        val transformer = TransformerFactory.newInstance().newTransformer()
        // some options to make the resulting files more readable
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        val input = DOMSource(doc)
        val output = StreamResult(file)

        transformer.transform(input, output)
    }

    abstract fun getRootElement(doc: Document): Element

    abstract fun parseItem(tag: Node, version: Int): T

    abstract fun convertItemToXml(item: T, doc: Document): Element

    protected fun update(fromVersion: Int) {
        val updatedVersion = updateInternal(fromVersion)

        if(updatedVersion == VERSION)
            save() // this will make updates persistent
        else
            TODO("Add error handling for failed updates")
    }

    protected abstract fun updateInternal(fromVersion: Int): Int

}