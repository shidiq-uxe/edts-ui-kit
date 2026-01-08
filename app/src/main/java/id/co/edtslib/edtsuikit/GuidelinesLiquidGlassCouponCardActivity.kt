package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesLiquidGlassCouponCardBinding

class GuidelinesLiquidGlassCouponCardActivity : GuidelinesBaseActivity() {

    private lateinit var binding: ActivityGuidelinesLiquidGlassCouponCardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGuidelinesLiquidGlassCouponCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val target = binding.blurTarget
        val card = binding.liquidGlassCard1

        card.setupBlur(this, target)
//        binding.liquidGlassCard1.apply {
//            title = "Test Title"
//            subtitle = "Test Subtitle"
//            badgeText = "10+"
//            startIconRes = R.drawable.ic_coupon_star_24
//            isEndIconVisible = false
//        }
    }
}