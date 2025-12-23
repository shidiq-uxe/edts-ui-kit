package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityButtonBinding
import id.co.edtslib.edtsuikit.helper.GuidelineItem
import id.co.edtslib.uikit.ribbon.Ribbon
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.setLightStatusBar
import id.co.edtslib.uikit.utils.snack

class GuidelinesButtonActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityButtonBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button)
        setLightStatusBar()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnFilled.setOnClickListener {
            Log.e("Button Debug", "Check for Double Click")
        }

        binding.stepperButton.setOnCountChangeListener {
            binding.root.snack("Count Changed to $it")

            binding.stepperButton.setPlusButtonEnabled(it < 5)
        }

        Ribbon(this).apply {
            ribbonText = "Ribbon"
            gravity = Ribbon.Gravity.START
            elevation = 4.dp
        }.anchorToView(
                rootParent = binding.root,
                targetView = binding.btnSecondaryOutlined,
                verticalAlignment = Ribbon.VerticalAlignment.Center
            )

        }
}