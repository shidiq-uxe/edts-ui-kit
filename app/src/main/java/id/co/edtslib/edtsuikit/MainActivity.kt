package id.co.edtslib.edtsuikit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.shape.RoundedRectangle
import id.co.edtslib.edtsuikit.databinding.ActivityMainBinding
import id.co.edtslib.uikit.boarding.Boarding
import id.co.edtslib.uikit.boarding.BoardingItemAlignment
import id.co.edtslib.uikit.boarding.IndicatorAlignment
import id.co.edtslib.uikit.databinding.ViewDialogBinding
import id.co.edtslib.uikit.indicator.IndicatorSlideMode
import id.co.edtslib.uikit.indicator.IndicatorStyle
import id.co.edtslib.uikit.utils.AlertType
import id.co.edtslib.uikit.utils.alertDialog
import id.co.edtslib.uikit.utils.alertSnack
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.setLightStatusBar
import id.co.edtslib.uikit.R as UIKitR

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityMainBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.setLightStatusBar()
        bindOnClickListener()

        val boardingView = binding.boardingView

        boardingView.autoScrollInterval = 3
        boardingView.items = List(3) {
            Boarding(
                image = drawable(UIKitR.drawable.ill_onboard_example),
                title = "Title $it",
                description = "${getString(R.string.lorem)} $it"
            )
        }

        // Set Horizontal Offset by 50% from the left screen
        // Set Vertical Offset by 60% from the top Screen
        boardingView.contentAlignment = BoardingItemAlignment(
            horizontalAlignmentPercent = 0.5f,
            verticalAlignmentPercent = 0.6f
        )
    }

    private val alertType = AlertType.entries.toList()

    private fun bindOnClickListener() {
        binding.btnTest.setOnClickListener(onClickCallback)
        binding.btnTest2.setOnClickListener(onClickCallback)
    }

    private val onClickCallback = View.OnClickListener { view ->
        when(view) {
            binding.btnTest -> {
                BoardingTray(this, supportFragmentManager).show {
                    this.alertDialog(
                        view = ViewDialogBinding.inflate(layoutInflater, null, false).apply { ->
                            this.btnPositive.setOnClickListener {
                                Intent(this@MainActivity, SpotlightTrialsActivity::class.java).let {
                                    startActivity(it)
                                }
                            }
                        }.root,
                    ).show()
                }
            }

            binding.btnTest2 -> {
                // Show Snack Bar Alert while anchor the alert on Extended View
                binding.btnTest.alertSnack(
                    message = getString(R.string.test_button_text),
                    alertType = alertType.random(),
                    isAnchored = true
                )
            }
        }
    }
}