package id.co.edtslib.edtsuikit

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import com.google.android.material.shape.MarkerEdgeTreatment
import com.google.android.material.shape.OffsetEdgeTreatment
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.RoundedRectangle
import id.co.edtslib.edtsuikit.databinding.ActivitySpotlightTrialsBinding
import id.co.edtslib.uikit.utils.TextStyle
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.minutes
import id.co.edtslib.uikit.utils.setLightStatusBar
import id.co.edtslib.uikit.utils.snack


class SpotlightTrialsActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivitySpotlightTrialsBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val actionBar = supportActionBar
        val colorDrawable = ColorDrawable(ContextCompat.getColor(this, R.color.white))
        actionBar?.setBackgroundDrawable(colorDrawable)
        actionBar?.elevation = 0f

        setContentView(R.layout.activity_spotlight_trials)

        setLightStatusBar()

        binding.root.doOnPreDraw {
            val cornerRadius = dimen(R.dimen.dimen_8dp)

            val markerEdgeTreatment = MarkerEdgeTreatment(cornerRadius.times(2))
            val offsetEdgeTreatment = OffsetEdgeTreatment(markerEdgeTreatment, 50f)

            binding.ivTarget1.setShapeAppearanceModel(
                binding.ivTarget1.shapeAppearanceModel
                    .toBuilder()
                    .setTopEdge(offsetEdgeTreatment)
                    .build()
            )
        }

        binding.ivTarget1.setOnClickListener {
            spotlightTrials()
        }

        binding.ivTarget2.setOnClickListener {
            binding.tilTest.isError = true
            binding.tilTest.error = "Email tidak ditemukan. Silakan masukkan nomor Handphone untuk melakukan pendaftaran"
        }


        val test1 = "Test Only"
        val test2 = "For Test Purpose"

        val message = getString(R.string.snk_hg_test, test1, test2)

        binding.tvTextTest.text = buildHighlightedMessage(
            context = this@SpotlightTrialsActivity,
            message = message,
            defaultTextAppearance = TextStyle.p1Style(this),
            highlightedTextAppearance = listOf(TextStyle.h2Style(this).copy(
                font = id.co.edtslib.uikit.R.font.inter_black
            ), TextStyle.errorStyle(this)),
            highlightedMessages = listOf(test1, test2),
            highlightClickAction = listOf(onHighlightClick(1), onHighlightClick(2))
        )

        binding.tvTextTest.movementMethod = LinkMovementMethod.getInstance()
        binding.tvTextTest.highlightColor = Color.TRANSPARENT

        binding.cdtv.displayAsHtml = true
        binding.cdtv.intervalInMillis = 3.minutes
        binding.cdtv.start()
    }

    private fun onHighlightClick(index: Int): (View) -> Unit = { binding.root.snack("Test $index") }

    private fun spotlightTrials() {
        val targets = mutableListOf<Target>()

        // first target
        val firstRoot = FrameLayout(this)
        val first = layoutInflater.inflate(R.layout.overlay_spotlight, firstRoot)

        binding.ivTarget1.doOnLayout { targetView ->
            val width = targetView.width.toFloat()
            val height = targetView.height.toFloat()

            val firstTarget = Target.Builder()
                .setAnchor(binding.ivTarget1)
                .setShape(
                    RoundedRectangle(
                        width = width,
                        height = height,
                        radius = resources.getDimension(R.dimen.dimen_8dp)
                    )
                )
                .setOverlay(first)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {
                    }

                    override fun onEnded() {
                    }
                })
                .build()

            targets.add(firstTarget)
        }

        // second target
        val secondRoot = FrameLayout(this)
        val second = layoutInflater.inflate(R.layout.overlay_spotlight, secondRoot)

        binding.ivTarget2.doOnLayout { targetView ->
            val width = targetView.width.toFloat()
            val height = targetView.height.toFloat()

            val secondTarget = Target.Builder()
                .setAnchor(binding.ivTarget2)
                .setShape(
                    RoundedRectangle(
                        width = width,
                        height = height,
                        radius = resources.getDimension(R.dimen.dimen_8dp)
                    )
                )
                .setOverlay(second)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {}

                    override fun onEnded() {}
                })
                .build()

            targets.add(secondTarget)
        }

        // third target
        val thirdRoot = FrameLayout(this)
        val third = layoutInflater.inflate(R.layout.overlay_spotlight, thirdRoot)

        binding.ivTarget3.doOnLayout { targetView ->
            val width = targetView.width.toFloat()
            val height = targetView.height.toFloat()

            val thirdTarget = Target.Builder()
                .setAnchor(binding.ivTarget3)
                .setShape(
                    RoundedRectangle(
                        width = width,
                        height = height,
                        radius = resources.getDimension(R.dimen.dimen_8dp)
                    )
                )
                .setOverlay(third)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {

                    }

                    override fun onEnded() {

                    }
                })
                .build()

            targets.add(thirdTarget)
        }

        // create spotlight
        val spotlight = Spotlight.Builder(this)
            .setTargets(targets)
            .setBackgroundColorRes(id.co.edtslib.uikit.R.color.spotlightBackground)
            .setDuration(500L)
            .setAnimation(DecelerateInterpolator(2f))
            .setOnSpotlightListener(object : OnSpotlightListener {
                override fun onStarted() {

                }

                override fun onEnded() {

                }
            })
            .build()

        val nextTarget = View.OnClickListener { spotlight.next() }
        val closeSpotlight = View.OnClickListener { spotlight.previous() }

        first.findViewById<View>(R.id.btnNext).setOnClickListener(nextTarget)
        second.findViewById<View>(R.id.btnNext).setOnClickListener(nextTarget)
        third.findViewById<View>(R.id.btnNext).setOnClickListener(nextTarget)

        first.findViewById<View>(R.id.btnPrevious).setOnClickListener(closeSpotlight)
        second.findViewById<View>(R.id.btnPrevious).setOnClickListener(closeSpotlight)
        third.findViewById<View>(R.id.btnPrevious).setOnClickListener(closeSpotlight)

        spotlight.start()
    }


}