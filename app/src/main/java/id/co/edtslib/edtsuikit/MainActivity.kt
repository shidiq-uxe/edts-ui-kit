package id.co.edtslib.edtsuikit

import android.content.Intent
import android.os.Bundle
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityMainBinding
import id.co.edtslib.uikit.boarding.Boarding
import id.co.edtslib.uikit.boarding.BoardingItemAlignment
import id.co.edtslib.uikit.databinding.ViewDialogBinding
import id.co.edtslib.uikit.popup.PopUp
import id.co.edtslib.uikit.utils.AlertType
import id.co.edtslib.uikit.utils.TextStyle
import id.co.edtslib.uikit.utils.alertSnack
import id.co.edtslib.uikit.utils.applyTextAppearanceSpan
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.font
import id.co.edtslib.uikit.utils.setLightStatusBar
import kotlin.math.roundToInt
import id.co.edtslib.uikit.R as UIKitR

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityMainBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    private val alertType = AlertType.values().toList()

    private fun bindOnClickListener() {
        binding.btnTest.setOnClickListener(onClickCallback)
        binding.btnTest2.setOnClickListener(onClickCallback)
    }

    private val dialogBinding get() = ViewDialogBinding.inflate(layoutInflater).apply {
        /*val parent = this.root
        val parentId = parent.id
        val positiveId = btnPositive.id
        val negativeId = btnNegative.id
        val messageId = tvMessage.id

        ConstraintSet().apply {
            clone(parent)

            // Clear previous constraints for buttons
            clear(id.co.edtslib.uikit.R.id.btnNegative)
            clear(id.co.edtslib.uikit.R.id.btnPositive)

            // Align buttons horizontally with respect to the parent
            connect(id.co.edtslib.uikit.R.id.btnNegative, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16.dp.roundToInt())
            connect(id.co.edtslib.uikit.R.id.btnPositive, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16.dp.roundToInt())

            // Set vertical constraints to align the buttons below the message
            connect(id.co.edtslib.uikit.R.id.btnNegative, ConstraintSet.TOP, id.co.edtslib.uikit.R.id.tvMessage, ConstraintSet.BOTTOM, 16.dp.roundToInt())
            connect(id.co.edtslib.uikit.R.id.btnNegative, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 16.dp.roundToInt())
            connect(id.co.edtslib.uikit.R.id.btnPositive, ConstraintSet.TOP, id.co.edtslib.uikit.R.id.tvMessage, ConstraintSet.BOTTOM, 16.dp.roundToInt())
            connect(id.co.edtslib.uikit.R.id.btnPositive, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 16.dp.roundToInt())

            // Create a horizontal chain between the buttons
            createHorizontalChain(
                id.co.edtslib.uikit.R.id.btnNegative, ConstraintSet.LEFT,
                id.co.edtslib.uikit.R.id.btnPositive, ConstraintSet.RIGHT,
                intArrayOf(id.co.edtslib.uikit.R.id.btnNegative, id.co.edtslib.uikit.R.id.btnPositive),
                null, // You can specify weights here if needed
                ConstraintSet.CHAIN_PACKED // Chain style: CHAIN_SPREAD, CHAIN_SPREAD_INSIDE, CHAIN_PACKED
            )

            // Set wrap content for width and height of the buttons
            constrainWidth(id.co.edtslib.uikit.R.id.btnNegative, ConstraintSet.WRAP_CONTENT)
            constrainWidth(id.co.edtslib.uikit.R.id.btnPositive, ConstraintSet.WRAP_CONTENT)
            constrainHeight(id.co.edtslib.uikit.R.id.btnNegative, ConstraintSet.WRAP_CONTENT)
            constrainHeight(id.co.edtslib.uikit.R.id.btnPositive, ConstraintSet.WRAP_CONTENT)

            // Apply the constraints to the ConstraintLayout
            applyTo(parent)
        }*/
    }


    private fun dialogMessage() = buildSpannedString {
        append("Nomor ")

        applyTextAppearanceSpan(this@MainActivity, TextStyle.h2Style(this@MainActivity)) {
            append("08112233445")
        }

        append(" belum terdaftar di Klik indomaret. Apa kamu ingin menggunakan nomor ini untuk membuat akun?")

    }

    private val onClickCallback = View.OnClickListener { view ->
        when(view) {
            binding.btnTest -> {
                BoardingTray(this, supportFragmentManager).show {
                    PopUp.show(
                        context = this,
                        isCentered = true,
                        title = "Title",
                        message = dialogMessage(),
                        positiveButton = "Positive CTA",
                        negativeButton = "Negative CTA",
                        onPositiveButtonClick = { view, dialog ->
                            startActivity(Intent(this, SpotlightTrialsActivity::class.java))
                        },
                        onNegativeButtonClick = { view, dialog ->
                            dialog.dismiss()

                            startActivity(Intent(this, ButtonActivity::class.java))
                        }
                    )
                }
            }

            binding.btnTest2 -> {
                // Show Snack Bar Alert while anchor the alert on Extended View
                binding.btnTest.alertSnack(
                    message = getString(R.string.test_button_text),
                    alertType = alertType.random(),
                    isAnchored = true
                )

                Intent(this, BoardingPlaygroundActivity::class.java).let {
                    startActivity(it)
                }
            }
        }
    }
}