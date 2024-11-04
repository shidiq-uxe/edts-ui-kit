package id.co.edtslib.edtsuikit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.util.FloatProperty
import android.view.View
import androidx.annotation.RequiresApi
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

class CircleSpringView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // Variables for circle position and size
    private var circleX = 100f // Initial X position
    private var circleY = 100f // Initial Y position
    private var radius = 50f   // Radius of the circle

    // Paint object for drawing the circle
    private val paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    // Define FloatProperties for circle positions
    private val circleXProperty = @RequiresApi(Build.VERSION_CODES.N)
    object : FloatPropertyCompat<CircleSpringView>("circleX") {
        override fun setValue(view: CircleSpringView, value: Float) {
            view.circleX = value
            view.invalidate()  // Redraw the view when the value is set
        }

        override fun getValue(view: CircleSpringView): Float {
            return view.circleX
        }
    }

    private val circleYProperty = @RequiresApi(Build.VERSION_CODES.N)
    object : FloatPropertyCompat<CircleSpringView>("circleY") {
        override fun setValue(view: CircleSpringView, value: Float) {
            view.circleY = value
            view.invalidate()  // Redraw the view when the value is set
        }

        override fun getValue(view: CircleSpringView): Float {
            return view.circleY
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Draw the circle at the updated coordinates
        canvas?.drawCircle(circleX, circleY, radius, paint)
    }

    // Start spring animation for circle position
    fun startSpringAnimation() {
        // Spring animation for the X position of the circle
        val springX = SpringAnimation(this, circleXProperty).apply {
            spring = SpringForce(500f).apply { // Target X position
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                stiffness = SpringForce.STIFFNESS_LOW
            }
            // setStartVelocity(1500f)
        }

        // Spring animation for the Y position of the circle
        val springY = SpringAnimation(this, circleYProperty).apply {
            spring = SpringForce(0f).apply { // Target Y position
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                stiffness = SpringForce.STIFFNESS_LOW
            }
            setStartVelocity(2000f)
        }

        // Start the animations
        // springX.start()
        springY.animateToFinalPosition(200f)
    }
}

