package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesAnimatedCardViewBinding
import id.co.edtslib.uikit.viewgroup.AnimatedCardView

class GuidelinesAnimatedCardViewActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesAnimatedCardViewBinding>()

    private var isShowing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_animated_card_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.root.setOnClickListener {
            if (isShowing) {
                isShowing = false
                binding.animatedCardViewHeader.animate(
                    type = AnimatedCardView.AnimationType.SLIDE,
                    mode = AnimatedCardView.AnimationMode.OUT,
                    slideDir = AnimatedCardView.SlideDirection.TOP,
                )

                binding.animatedCardViewFooter.animate(
                    type = AnimatedCardView.AnimationType.SLIDE,
                    mode = AnimatedCardView.AnimationMode.OUT,
                    slideDir = AnimatedCardView.SlideDirection.BOTTOM,
                )

                binding.animatedCardViewFade.animate(
                    type = AnimatedCardView.AnimationType.FADE,
                    mode = AnimatedCardView.AnimationMode.OUT,
                )

            } else {
                isShowing = true
                binding.animatedCardViewHeader.animate(
                    type = AnimatedCardView.AnimationType.SLIDE,
                    mode = AnimatedCardView.AnimationMode.IN,
                    slideDir = AnimatedCardView.SlideDirection.TOP,
                )

                binding.animatedCardViewFooter.animate(
                    type = AnimatedCardView.AnimationType.SLIDE,
                    mode = AnimatedCardView.AnimationMode.IN,
                    slideDir = AnimatedCardView.SlideDirection.BOTTOM,
                )

                binding.animatedCardViewFade.animate(
                    type = AnimatedCardView.AnimationType.FADE,
                    mode = AnimatedCardView.AnimationMode.IN,
                )
            }
        }
    }
}