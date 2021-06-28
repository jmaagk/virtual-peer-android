package me.maagk.johannes.virtualpeer.view

/**
 * This interface is used to control the addition of a default listener in *QuestionView classes in this package.
 * It adds a property to let the user of theses classes decide whether the default listener
 * (and therefore behavior) should be added to these View classes.
 *
 * This is useful because things like surveys don't need specific control over this behavior. However,
 * things like the chat control the answering flow on their own and hence need custom listeners.
 */
interface DefaultListenerController {

    var setDefaultListener: Boolean

}