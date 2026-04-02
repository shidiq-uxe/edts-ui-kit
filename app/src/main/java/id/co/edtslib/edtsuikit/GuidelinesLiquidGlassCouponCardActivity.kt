package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesLiquidGlassCouponCardBinding

class GuidelinesLiquidGlassCouponCardActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesLiquidGlassCouponCardBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            listOf(
                liquidGlassCard1,
                liquidGlassCard2,
                liquidGlassCard3,
                liquidGlassCard4
            ).forEach { card ->
                card.setupBlur(this@GuidelinesLiquidGlassCouponCardActivity, blurTarget)
            }
        }

    }
}