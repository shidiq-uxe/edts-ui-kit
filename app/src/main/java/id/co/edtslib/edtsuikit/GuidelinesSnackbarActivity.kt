package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesSnackbarBinding
import id.co.edtslib.uikit.utils.AlertType
import id.co.edtslib.uikit.utils.alertSnack

class GuidelinesSnackbarActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesSnackbarBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_snackbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnShowSnackbar.setOnClickListener {
            showSnackbar()
        }

    }

    private fun showSnackbar() {
        val isError = binding.rgType.checkedRadioButtonId == R.id.rbError
        val isMultiline = binding.rgLines.checkedRadioButtonId == R.id.rbMultiline
        val shouldShowIconAndAction = binding.cbIncludeAction.isChecked
        val isFade = binding.rgAnimation.checkedRadioButtonId == R.id.rbFade

        binding.btnShowSnackbar.alertSnack(
            message = if (isMultiline) "This would be a multiline Text Snackbar that fill more lines than single line snackbar" else "This is a Snackbar",
            alertType = if (isError) AlertType.ERROR else AlertType.DEFAULT,
            actionText = if (shouldShowIconAndAction) "Action" else null,
            startIconRes = if (shouldShowIconAndAction) id.co.edtslib.uikit.R.drawable.ic_placeholder_medium_24 else null,
            animationMode = if (isFade) Snackbar.ANIMATION_MODE_FADE else Snackbar.ANIMATION_MODE_SLIDE,
            isAnchored = true
        )

    }
}