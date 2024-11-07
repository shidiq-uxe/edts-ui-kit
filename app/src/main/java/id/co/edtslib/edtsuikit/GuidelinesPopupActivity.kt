package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesPopupBinding
import id.co.edtslib.uikit.popup.PopUp

class GuidelinesPopupActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesPopupBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_guidelines_popup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initOnClickListener()
    }

    private fun initOnClickListener() {
        binding.btnOneButtonDialog.setOnClickListener {
            showOneButtonDialog()
        }

        binding.btnTwoButtonDialog.setOnClickListener {
            showTwoButtonDialog()
        }

        binding.btnHorizontalButtonDialog.setOnClickListener {
            showStackedButton()
        }
    }

    private fun showOneButtonDialog() {
        PopUp.show(
            context = this,
            isCentered = true,
            title = "Dialog Title",
            message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc condimentum orci sodales eros? ",
            positiveButton = "Action Button",
            onBindView = {

            },
            onPositiveButtonClick = { _, dialog ->
                dialog.dismiss()
            },
        )
    }

    private fun showTwoButtonDialog() {
        PopUp.show(
            context = this,
            isCentered = true,
            title = "Dialog Title",
            message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc condimentum orci sodales eros? ",
            positiveButton = "Positive Button",
            negativeButton = "Negative Button",
            onBindView = {

            },
            onPositiveButtonClick = { _, dialog ->
                dialog.dismiss()
            },
            onNegativeButtonClick = { _, dialog ->
                dialog.dismiss()
            }
        )
    }

    private fun showStackedButton() {
        PopUp.show(
            context = this,
            isCentered = false,
            title = "Dialog Title",
            message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc condimentum orci sodales eros? ",
            isActionHorizontal = false,
            positiveButton = "Positive Button",
            negativeButton = "Negative Button",
            onBindView = {

            },
            onPositiveButtonClick = { _, dialog ->
                dialog.dismiss()
            },
            onNegativeButtonClick = { _, dialog ->
                dialog.dismiss()
            }
        )
    }
}