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
        val card1 = binding.liquidGlassCard1
        val card2 = binding.liquidGlassCard2
        val card3 = binding.liquidGlassCard3
        val card4 = binding.liquidGlassCard4

        card1.setupBlur(this, target)
        card2.setupBlur(this, target)
        card3.setupBlur(this, target)
        card4.setupBlur(this, target)
    }
}