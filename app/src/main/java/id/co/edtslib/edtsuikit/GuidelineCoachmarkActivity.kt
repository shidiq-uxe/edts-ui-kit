package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.RoundedRectangle
import com.takusemba.spotlight.shape.Shape
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelineCoachmarkBinding
import id.co.edtslib.uikit.coachmark.CoachMarkData
import id.co.edtslib.uikit.coachmark.CoachMarkOverlay
import id.co.edtslib.uikit.coachmark.CoachmarkDelegate
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.setLightStatusBar

class GuidelineCoachmarkActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityGuidelineCoachmarkBinding>()

    private lateinit var coachMarkOverlay: CoachMarkOverlay

    private val coachmarkItems = mutableListOf<CoachMarkData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guideline_coachmark)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = color(id.co.edtslib.uikit.R.color.primary_30)

        addDummyTargets()

        coachMarkOverlay = CoachMarkOverlay(this)

        Handler(Looper.getMainLooper()).postDelayed( {

            addContentView(
                coachMarkOverlay,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )

            coachMarkOverlay.coachMarkDelegate = object : CoachmarkDelegate {
                override fun onNextClickClickListener() {

                }

                override fun onSkipClickListener() {}

                override fun onDismissListener() {

                }
            }

            if (coachmarkItems.isNotEmpty()) {
                coachMarkOverlay.setCoachMarkItems(coachmarkItems)
            }
        } , 1000L)
    }

    private fun customSpotlightTest() {

    }

    private fun addDummyTargets() {
        coachmarkItems.add(
            CoachMarkData(
                title = "Spotlight One",
                description = "This is a short description showing what Spotlight One highlight",
                target = binding.sbHome
            )
        )

        coachmarkItems.add(
            CoachMarkData(
                title = "Spotlight 2",
                description = "This is a short description showing what Spotlight Two highlight",
                target = binding.homeSwitcher
            )
        )

        coachmarkItems.add(
            CoachMarkData(
                title = "Spotlight 3",
                description = "This is a short description showing what Spotlight Three highlight",
                target = binding.tlHome
            )
        )
    }

    private fun roundedTargetShape(
        width: Int,
        height: Int
    ): Shape = RoundedRectangle(
        width = width.toFloat(),
        height = height.toFloat(),
        radius = 8.dp
    )

    private fun spotlightTest() {
        val frameTarget = FrameLayout(this)
        val coachmarkOverlay = layoutInflater.inflate(R.layout.overlay_spotlight, frameTarget)

        binding.sbHome.doOnPreDraw { targetView ->
            val targetWidth = targetView.width.plus(8.dp).toInt()
            val targetHeight = targetView.height.plus(8.dp).toInt()

            val target = Target.Builder()
                .setAnchor(binding.sbHome)
                .setShape(roundedTargetShape(targetWidth, targetHeight))
                .setOverlay(coachmarkOverlay)
                .build()

            val spotlight = Spotlight.Builder(this)
                .setTargets(target)
                .setBackgroundColorRes(id.co.edtslib.uikit.R.color.spotlightBackground)
                .setDuration(500L)
                .setAnimation(DecelerateInterpolator(2f))
                .build()

            spotlight.start()
        }
    }

}