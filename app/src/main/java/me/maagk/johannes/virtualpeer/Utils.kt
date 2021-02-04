package me.maagk.johannes.virtualpeer

import android.content.Context
import android.util.Log
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat

class Utils {

    companion object {

        fun log(message: String) {
            Log.d("Virtual Peer", message)
        }

        fun getColor(context: Context, @ColorRes colorId: Int): Int {
            return ResourcesCompat.getColor(context.resources, colorId, context.theme)
        }

    }

}