package me.maagk.johannes.virtualpeer.misc

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

class CustomGridLayoutManager(context: Context, spanCount: Int) : GridLayoutManager(context, spanCount) {

    var scrollingEnabled = true

    override fun canScrollHorizontally(): Boolean {
        return scrollingEnabled && super.canScrollHorizontally()
    }

    override fun canScrollVertically(): Boolean {
        return scrollingEnabled && super.canScrollVertically()
    }

}