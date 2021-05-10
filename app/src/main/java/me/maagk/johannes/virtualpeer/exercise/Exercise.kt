package me.maagk.johannes.virtualpeer.exercise

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment

abstract class Exercise(protected val context: Context, val name: String, val info: String,
                        @ColorRes color: Int, @ColorRes textColor: Int, @ColorRes buttonTextColor: Int,
                        var pinned: Boolean = false) {

    val color = Utils.getColor(context, color)
    val textColor = Utils.getColor(context, textColor)
    val buttonTextColor = Utils.getColor(context, buttonTextColor)

    private lateinit var chatExercise: ChatExercise

    protected abstract fun createChatExercise(chatFragment: ChatFragment): ChatExercise

    fun getChatExercise(chatFragment: ChatFragment): ChatExercise {
        if(!::chatExercise.isInitialized)
            chatExercise = createChatExercise(chatFragment)

        return chatExercise
    }

    @DrawableRes
    abstract fun getIconResourceId(): Int

    fun getIcon(): Drawable? = ResourcesCompat.getDrawable(context.resources, getIconResourceId(), context.theme)

}