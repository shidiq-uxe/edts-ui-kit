package id.co.edtslib.uikit.tablayout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.use
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.R as UIKitR


internal class HomeTabLayoutBottomEdges @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val path = Path()
    private val paint = Paint()

    var gravity = EdgesGravity.START
        set(value) {
            field = value
            invalidate()
        }

    init {
        paint.color = context.color(UIKitR.color.primary_30)
        paint.style = Paint.Style.FILL

        context.obtainStyledAttributes(attrs, UIKitR.styleable.SegmentedTabLayoutBottomEdges).use {
            gravity = when (it.getInt(UIKitR.styleable.SegmentedTabLayoutBottomEdges_edgeGravity, 0)) {
                0 -> EdgesGravity.START
                1 -> EdgesGravity.END
                else -> EdgesGravity.START
            }
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        path.reset()

        val width = w.toFloat()
        val height = h.toFloat()

        if (gravity == EdgesGravity.START) {
            // Starting point
            path.moveTo(0f, 0f)
            // Left vertical line
            path.lineTo(0f, height)

            // The crucial curve - using quadTo for this specific curve shape
            path.quadTo(
                width * 0.1f,  // control point X - slightly offset for smooth curve
                height * 0.1f, // control point Y - creates the right curvature
                width,         // end point X
                0f            // end point Y
            )

            // Close the path
            path.lineTo(0f, 0f)
            path.close()
        } else {
            // Starting point
            path.moveTo(width, 0f)
            // Left vertical line
            path.lineTo(width, height)
            // The crucial curve - using quadTo for this specific curve shape
            path.quadTo(
                width * 0.9f,  // control point X - slightly offset for smooth curve
                height * 0.1f, // control point Y - creates the right curvature
                0f,            // end point X
                0f            // end point Y
            )
            // Close the path
            path.lineTo(width, 0f)
            path.close()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    enum class EdgesGravity {
        START,
        END
    }
}
