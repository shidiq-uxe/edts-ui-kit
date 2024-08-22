package id.co.edtslib.edtsuikit

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MarkerEdgeTreatment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.OffsetEdgeTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.RoundedRectangle
import id.co.edtslib.edtsuikit.databinding.ActivitySpotlightTrialsBinding
import id.co.edtslib.uikit.textinputlayout.helper.AsteriskPasswordTransformationMethod
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.setLightStatusBar
import java.nio.file.Files.size
import kotlin.math.roundToInt
import id.co.edtslib.uikit.R as UIKitR


class SpotlightTrialsActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivitySpotlightTrialsBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val actionBar = supportActionBar
        val colorDrawable = ColorDrawable(ContextCompat.getColor(this, R.color.white))
        actionBar?.setBackgroundDrawable(colorDrawable)

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
            binding.tilTest.isError = !binding.tilTest.isError
            binding.tilTest.error = "Email tidak ditemukan. Silakan masukkan nomor Handphone untuk melakukan pendaftaran"
        }

        binding.tilTest.isError = true
    }


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