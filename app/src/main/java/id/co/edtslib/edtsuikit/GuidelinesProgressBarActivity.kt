package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesProgressBarBinding
import id.co.edtslib.uikit.progressbar.LinearProgressBar
import kotlin.random.Random

class GuidelinesProgressBarActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesProgressBarBinding>()
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_progress_bar)

        binding.btnProgressSingleLap.setOnClickListener {
            binding.gLPBSingleLap.indicatorProgress += 40f
        }

        binding.gLPBMultiLap.progressLimit = 4000f
        binding.gLPBMultiLap.showBadge = true
        binding.btnProgressMultiLap.setOnClickListener {
            binding.gLPBMultiLap.indicatorProgress += 40f
        }

        binding.btnProgressLoading.setOnClickListener {
            startRandomProgressSimulation(binding.gLPBLoading, minStep = 1, maxStep = 10)
        }

        binding.gLPBDisabled.isEnabled = false
        binding.gLPBDisabled.indicatorProgress = 50f

        binding.gLPBIntermittentFixed.startIntermittentAnimation(LinearProgressBar.IntermittentMode.FIXED_WIDTH)
        binding.gLPBIntermittentStretch.startIntermittentAnimation(LinearProgressBar.IntermittentMode.STRETCH)


        binding.gCPB.indicatorProgress = 80f
        binding.btnVisibility.setOnClickListener {
            if (binding.gCPB.indicatorProgress >= 80) {
                binding.gCPB.indicatorProgress = 20f
            } else {
                binding.gCPB.indicatorProgress = 80f
            }
        }
    }

    private fun startRandomProgressSimulation(
        progressIndicator: LinearProgressBar,
        minStep: Int = 1,
        maxStep: Int = 8,
    ) {
        stopProgressSimulation()
        progressIndicator.setLoadingStarted()

        val random = Random.Default

        progressRunnable = object : Runnable {
            override fun run() {
                progressIndicator.indicatorProgress +=
                    random.nextInt(minStep, maxStep).toFloat()

                if (progressIndicator.indicatorProgress < progressIndicator.progressLimit) {
                    handler.postDelayed(
                        this,
                        500L
                    )
                } else {
                    progressIndicator.setLoadingFinished()
                }
            }
        }

        handler.post(progressRunnable!!)
    }

    private fun stopProgressSimulation(progressIndicator: LinearProgressBar? = null) {
        progressRunnable?.let { handler.removeCallbacks(it) }
        progressRunnable = null
        progressIndicator?.setLoadingFinished()
    }

}