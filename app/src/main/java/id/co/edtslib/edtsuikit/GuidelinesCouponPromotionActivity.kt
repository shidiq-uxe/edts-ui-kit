package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesCouponPromotionBinding
import id.co.edtslib.uikit.utils.window.WindowInsetsConfig

class GuidelinesCouponPromotionActivity : GuidelinesBaseActivity() {

    override val windowInsetsConfig = WindowInsetsConfig.Custom { v, insets ->
        val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
        insets
    }

    private val binding by viewBinding<ActivityGuidelinesCouponPromotionBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }
}