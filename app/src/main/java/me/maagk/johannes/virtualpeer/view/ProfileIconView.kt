package me.maagk.johannes.virtualpeer.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.FrameLayout
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils

class ProfileIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private val textPaint = Paint()
    private val textBounds = Rect()

    var usernameChar = ' '
        set(value) {
            field = value
            invalidate()
        }

    init {
        paint.isAntiAlias = true
        paint.color = Utils.getColor(context, R.color.colorPrimary)

        textPaint.isAntiAlias = true
        textPaint.color = Utils.getColor(context, R.color.colorText)
        textPaint.textSize = Utils.dpToPx(16f, context.resources.displayMetrics)
        textPaint.isSubpixelText = true
        textPaint.textAlign = Paint.Align.CENTER

        // this allows this View to draw on its own
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (width / 2).toFloat(), paint)

        val usernameCharString = usernameChar.toString()
        textPaint.getTextBounds(usernameCharString, 0, 1, textBounds)

        canvas?.drawText(usernameCharString, (width / 2).toFloat(), ((height / 2) + (textBounds.height() / 2)).toFloat(), textPaint)
    }

}