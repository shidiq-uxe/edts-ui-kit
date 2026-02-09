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
                liquidGlassCard4,
                liquidGlassCard5,
                liquidGlassCard6
            ).forEach { card ->
                card.setupBlur(this@GuidelinesLiquidGlassCouponCardActivity, blurTarget)
            }
        }

        binding.liquidGlassCard5.setupBlur(this, binding.blurTarget)

        binding.liquidGlassCard5.isBadgeLoading = true
        binding.root.postDelayed({
            binding.liquidGlassCard5.isBadgeLoading = false
            binding.liquidGlassCard5.badgeText = "10+"
        }, 10000)

        binding.liquidGlassCard6.setupBlur(this, binding.blurTarget)

        binding.liquidGlassCard6.isBadgeLoading = true
        binding.root.postDelayed({
            binding.liquidGlassCard6.isBadgeLoading = false
            binding.liquidGlassCard6.badgeText = "1"
        }, 10000)
    }
}