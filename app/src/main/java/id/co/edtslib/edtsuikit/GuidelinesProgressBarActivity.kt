package id.co.edtslib.edtsuikit

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.LinearProgressIndicator
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesProgressBarBinding
import id.co.edtslib.uikit.progressbar.GradientProgressBarDelegate

class GuidelinesProgressBarActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesProgressBarBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_progress_bar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.gLPB.delegate = object : GradientProgressBarDelegate {
            override fun onAnimationUpdateListener(
                view: View,
                currentProgressValue: Float,
                finalProgressValue: Float
            ) {
                Log.e(this@GuidelinesProgressBarActivity.javaClass.simpleName, "Progress Value : $currentProgressValue, $finalProgressValue")
            }
        }

        binding.gLPB.indicatorProgress = 80f
        binding.gCPB.indicatorProgress = 80f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.lpiSample.setProgress(80, true)
        }

        binding.lpiSample.showAnimationBehavior = LinearProgressIndicator.SHOW_OUTWARD
        binding.lpiSample.hideAnimationBehavior = LinearProgressIndicator.HIDE_OUTWARD

        binding.btnVisibility.setOnClickListener {
            // if (binding.lpiSample.isVisible) binding.lpiSample.hide() else binding.lpiSample.show()

            if (binding.gLPB.indicatorProgress >= 80) {
                binding.gLPB.indicatorProgress = 20f
            } else {
                binding.gLPB.indicatorProgress = 80f
            }

            if (binding.gCPB.indicatorProgress >= 80) {
                binding.gCPB.indicatorProgress = 20f
            } else {
                binding.gCPB.indicatorProgress = 80f
            }

            /*if (binding.piSample.isRunning) {
                binding.piSample.stopAnimation()
            } else {
                binding.piSample.startAnimation()
            }*/

            if (binding.lpiSample.progress >= 80) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.lpiSample.setProgress(20, true)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.lpiSample.setProgress(80, true)
                }
            }
        }
    }
}