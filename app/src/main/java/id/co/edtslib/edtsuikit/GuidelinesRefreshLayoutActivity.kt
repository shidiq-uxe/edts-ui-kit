package id.co.edtslib.edtsuikit

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.Interpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesRefreshLayoutBinding
import kotlin.math.exp

class GuidelinesRefreshLayoutActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityGuidelinesRefreshLayoutBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_refresh_layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnSpringForce.setOnClickListener {
            binding.springView.startSpringAnimation()

            //startSpringAnimation()

            /*SpringAnimation(binding.btnSpringForce, SpringAnimation.TRANSLATION_Y, 0f).apply {
                setStartVelocity(-3000f)

                spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                spring.stiffness = SpringForce.STIFFNESS_LOW
            }.addUpdateListener { animation, value, velocity ->

            }.start()*/

            binding.btnSpringForce.doOnLayout {
                val springY = SpringAnimation(FloatValueHolder(binding.btnSpringForce.layoutParams.height.toFloat())).apply {
                    spring = SpringForce(0f).apply {
                        dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                        stiffness = SpringForce.STIFFNESS_LOW
                    }
                    addUpdateListener { _, value, _ ->
                        binding.btnSpringForce.translationY = value
                    }
                }

                springY.setStartVelocity(5000f).start()
            }


        }
    }

    private fun startSpringAnimation() {
        val targetPosition = 0f
        val initialPosition = 100f
        val springForce = SpringForce(targetPosition).apply {
            stiffness = SpringForce.STIFFNESS_VERY_LOW
            dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
        }

        // Set initial velocity and position
        var velocity = 0f
        var currentPosition = initialPosition

        // Start the value animator that will simulate the spring animation
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 500 // Duration doesn't matter too much, we're controlling the animation frame by frame

            addUpdateListener { animation ->
                val deltaTime = 16f / duration // Approximate time between frames (16ms)

                // Calculate the spring force acting on the current position
                val force = springForce.force(currentPosition, targetPosition)

                // Update velocity and position using the force (simple Euler integration)
                velocity += force * deltaTime
                currentPosition += velocity * deltaTime

                // Update the view's translationY
                binding.btnSpringForce.translationY = currentPosition

                // Stop the animator if the velocity is small and the position is near the target
                if (Math.abs(currentPosition - targetPosition) < 0.1f && Math.abs(velocity) < 0.1f) {
                    cancel() // End the animation
                }
            }

            start()
        }
    }

    // Extension function to calculate the spring force manually
    private fun SpringForce.force(currentPosition: Float, targetPosition: Float): Float {
        return -stiffness * (currentPosition - targetPosition) - dampingRatio * currentPosition
    }

}