package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.FrameLayout
import androidx.core.view.isVisible
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesProgressBarBinding
import id.co.edtslib.uikit.core.linearprogressbar.CoreLinearProgressBar
import id.co.edtslib.uikit.utils.dp
import kotlin.random.Random

class GuidelinesProgressBarActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesProgressBarBinding>()
    private val handler = Handler(Looper.getMainLooper())
    private val simulations = HashMap<CoreLinearProgressBar, Runnable>()

    override val hasConfigMenu = true

    private var config = ProgressBarConfig()
    private var previewBar: CoreLinearProgressBar? = null
    private var previewKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = savedInstanceState?.getBundle(STATE_CONFIG)?.toProgressBarConfig() ?: ProgressBarConfig()
        supportFragmentManager.setFragmentResultListener(
            LinearProgressBarConfigBottomSheet.RESULT_CONFIG,
            this,
        ) { _, result ->
            config = result.toProgressBarConfig()
            updatePreview()
        }

        setContentView(binding.root)
        setupPreviewAction()
        setupSingleLap()
        setupMultiLap()
        setupLoading()
        setupDisabled()
        setupIntermittent()
        setupCircular()

        updatePreview()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(STATE_CONFIG, config.toBundle())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        simulations.values.forEach { handler.removeCallbacks(it) }
        simulations.clear()
        super.onDestroy()
    }

    override fun onConfigMenuClicked() {
        showConfigSheet()
    }

    private fun setupPreviewAction() {
        binding.btnPreviewAnimate.setOnClickListener {
            val bar = previewBar ?: return@setOnClickListener
            when (config.mode) {
                ProgressBarMode.SINGLE_LAP, ProgressBarMode.MULTI_LAP -> {
                    bar.indicatorProgress =
                        if (bar.indicatorProgress >= bar.progressLimit) 0f
                        else bar.indicatorProgress + PROGRESS_STEP
                }
                ProgressBarMode.LOADING -> {
                    bar.indicatorProgress = 0f
                    startRandomProgressSimulation(bar, minStep = 1, maxStep = 10)
                }
                else -> Unit
            }
        }
    }

    private fun updatePreview() {
        val existing = previewBar?.takeIf { previewKey == config.previewKey }
        val bar = existing ?: rebuildPreview()

        applyConfig(bar)
        if (existing == null) startModeBehaviour(bar)

        binding.btnPreviewAnimate.isVisible = config.mode.hasAnimateAction
    }

    private fun rebuildPreview(): CoreLinearProgressBar {
        previewBar?.let { stopProgressSimulation(it) }

        val container = binding.previewContainer
        container.removeAllViews()

        val bar = config.product.create(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ).apply { gravity = Gravity.CENTER_VERTICAL }
        }
        container.addView(bar)

        previewBar = bar
        previewKey = config.previewKey
        return bar
    }

    private fun applyConfig(bar: CoreLinearProgressBar) {
        bar.isEnabled = !config.disabled

        bar.progressLimit =
            if (config.mode == ProgressBarMode.MULTI_LAP) bar.maxIndicatorProgress * MULTI_LAP_COUNT
            else bar.maxIndicatorProgress

        val showBadge = config.showBadge ?: if (config.mode == ProgressBarMode.MULTI_LAP) true else null
        showBadge?.let { bar.showBadge = it }

        config.thicknessDp?.let {
            bar.trackThickness = it.toFloat().dp
            bar.trackCornerRadius = bar.trackThickness / 2f
        }

        config.showThumb?.let { bar.showThumb = it }
        config.thumbSizeDp?.let {
            bar.thumbSize = it.toFloat().dp
            bar.thumbCornerRadius = bar.thumbSize / 2f
        }

        config.showInnerShadow?.let { bar.showInnerShadow = it }
        config.shadowOffsetX?.let { bar.innerShadowOffsetX = it }
        config.shadowOffsetY?.let { bar.innerShadowOffsetY = it }
        config.shadowSpread?.let { bar.innerShadowSpread = it }

        config.trackColor?.let { bar.trackColor = it }
        config.indicatorColor?.let { bar.indicatorColor = it }
        config.thumbColor?.let { bar.thumbColor = it }
        config.completedColor?.let { bar.completedIndicatorColor = it }

        if (config.mode.usesProgress) {
            bar.indicatorProgress = bar.progressLimit * config.progress / 100f
        }
    }

    private fun startModeBehaviour(bar: CoreLinearProgressBar) {
        when (config.mode) {
            ProgressBarMode.LOADING ->
                startRandomProgressSimulation(bar, minStep = 1, maxStep = 10)
            ProgressBarMode.INTERMITTENT_FIXED ->
                bar.startIntermittentAnimation(CoreLinearProgressBar.IntermittentMode.FIXED_WIDTH)
            ProgressBarMode.INTERMITTENT_STRETCH ->
                bar.startIntermittentAnimation(CoreLinearProgressBar.IntermittentMode.STRETCH)
            else -> Unit
        }
    }

    private fun showConfigSheet() {
        if (supportFragmentManager.findFragmentByTag(LinearProgressBarConfigBottomSheet.TAG) != null) return
        LinearProgressBarConfigBottomSheet.newInstance(config)
            .show(supportFragmentManager, LinearProgressBarConfigBottomSheet.TAG)
    }

    private fun setupSingleLap() = with(binding) {
        btnProgressSingleLap.setOnClickListener {
            gLPBSingleLap.indicatorProgress += PROGRESS_STEP
        }
    }

    private fun setupMultiLap() = with(binding) {
        gLPBMultiLap.progressLimit = 4000f
        gLPBMultiLap.showBadge = true
        btnProgressMultiLap.setOnClickListener {
            gLPBMultiLap.indicatorProgress += PROGRESS_STEP
        }
    }

    private fun setupLoading() = with(binding) {
        btnProgressLoading.setOnClickListener {
            startRandomProgressSimulation(gLPBLoading, minStep = 1, maxStep = 10)
        }
    }

    private fun setupDisabled() = with(binding) {
        gLPBDisabled.isEnabled = false
        gLPBDisabled.indicatorProgress = 50f
    }

    private fun setupIntermittent() = with(binding) {
        gLPBIntermittentFixed.startIntermittentAnimation(CoreLinearProgressBar.IntermittentMode.FIXED_WIDTH)
        gLPBIntermittentStretch.startIntermittentAnimation(CoreLinearProgressBar.IntermittentMode.STRETCH)
    }

    private fun setupCircular() = with(binding) {
        gCPB.indicatorProgress = 80f
        btnVisibility.setOnClickListener {
            gCPB.indicatorProgress = if (gCPB.indicatorProgress >= 80) 20f else 80f
        }
    }

    private fun startRandomProgressSimulation(
        bar: CoreLinearProgressBar,
        minStep: Int = 1,
        maxStep: Int = 8,
    ) {
        stopProgressSimulation(bar)
        bar.setLoadingStarted()

        val runnable = object : Runnable {
            override fun run() {
                bar.indicatorProgress += Random.nextInt(minStep, maxStep).toFloat()

                if (bar.indicatorProgress < bar.progressLimit) {
                    handler.postDelayed(this, SIMULATION_STEP_DELAY_MS)
                } else {
                    simulations.remove(bar)
                    bar.setLoadingFinished()
                }
            }
        }

        simulations[bar] = runnable
        handler.post(runnable)
    }

    private fun stopProgressSimulation(bar: CoreLinearProgressBar, finishLoading: Boolean = false) {
        simulations.remove(bar)?.let { handler.removeCallbacks(it) }
        if (finishLoading) bar.setLoadingFinished()
    }

    companion object {
        private const val STATE_CONFIG = "progress_bar_config_state"
        private const val PROGRESS_STEP = 40f
        private const val MULTI_LAP_COUNT = 4
        private const val SIMULATION_STEP_DELAY_MS = 500L
    }
}