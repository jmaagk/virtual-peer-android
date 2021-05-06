package me.maagk.johannes.virtualpeer.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import kotlin.math.sin

class EyeView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()

    private val startAngleBottom = 22.5f
    private val startAngleTop = 202.5f
    private val sweepAngle = 135f

    var expansion: Float = 1f
        set(value) {
            field = value
            invalidate()
        }

    init {
        paint.isAntiAlias = true
        paint.color = Utils.getColor(context, R.color.colorExerciseMeditationGradientTo)

        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // this function will calculate at which y value an arc ends
        fun getArcEndPointY(startAngle: Float, sweepAngle: Float, centerY: Float, radius: Float): Float {
            val edgeAngle = startAngle + sweepAngle // the point at which the sweep cuts off
            return (centerY + radius * sin(Math.toRadians(edgeAngle.toDouble()))).toFloat()
        }

        // the total height of this view
        val totalHeight = height.toFloat()

        // the height adjusted for the amount of expansion this view is currently set to
        val totalHeightAdjusted = totalHeight * expansion

        // the center of this view
        val center = totalHeight / 2

        // the radius of the two arcs being drawn; changes with expansion
        val radius = totalHeightAdjusted / 2

        // the total width of this view; always stays the same
        val width = width.toFloat()

        /*
         * bottom half
         * moving the arc around by its distance to the center of the view to always keep the sharp line
         * at the top in the same place
         */
        var distanceToCenter = getArcEndPointY(startAngleBottom, sweepAngle, center, radius) - center
        canvas?.drawArc(
                0f,
                center - radius - distanceToCenter,
                width,
                center + radius - distanceToCenter,
                startAngleBottom, sweepAngle, false, paint)

        // top half; see above for more info
        distanceToCenter = center - getArcEndPointY(startAngleTop, sweepAngle, center, radius)
        canvas?.drawArc(
                0f,
                center - radius + distanceToCenter,
                width,
                center + radius + distanceToCenter,
                startAngleTop, sweepAngle, false, paint)
    }

    fun close(duration: Long) {
        ObjectAnimator.ofFloat(this, "expansion", 0f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

}