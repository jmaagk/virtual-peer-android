package me.maagk.johannes.virtualpeer.goals

import android.content.Context
import java.util.*

class GoalStorage(context: Context) {

    companion object {
        const val ID_LENGTH = 32
    }

    fun generateNewId(): String {
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = upper.toLowerCase(Locale.ROOT)
        val numbers = "0123456789"
        val charset = upper + lower + numbers

        val random = Random()

        fun generate(): String {
            val output = StringBuilder()
            for(i in 0 until ID_LENGTH)
                output.append(charset[random.nextInt(charset.length)])
            return output.toString()
        }

        var output = ""
        do {
            output = generate()
        } while(!validateId(output))

        return output
    }

    fun validateId(id: String): Boolean {
        // TODO: implement this
        return true
    }

}