package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesChipBinding
import id.co.edtslib.uikit.core.chip.CHIP_STATE_ACTIVE
import id.co.edtslib.uikit.core.chip.CHIP_STATE_IDLE
import id.co.edtslib.uikit.core.chip.ChipDelegate
import id.co.edtslib.uikit.core.chip.CoreChip
import id.co.edtslib.uikit.recipes.chip.klik.KlikChip
import id.co.edtslib.uikit.recipes.chip.klik.KlikChipVariant
import id.co.edtslib.uikit.recipes.chip.poinku.PoinkuChip
import android.widget.Toast
import id.co.edtslib.uikit.core.chip.ChipStateAnnotation
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesChipActivity : GuidelinesBaseActivity() {
    private val binding by viewBinding<ActivityGuidelinesChipBinding>()
    override val hasConfigMenu = true
    private var config = ChipConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = savedInstanceState?.getBundle(STATE_CONFIG)?.toChipConfig() ?: ChipConfig()
        supportFragmentManager.setFragmentResultListener(
            ChipConfigBottomSheet.RESULT_CONFIG,
            this,
        ) { _, result ->
            config = result.toChipConfig()
            updatePreview()
        }

        setContentView(binding.root)

        setupCoreDefault()
        setupKlikStates()
        setupKlikFiltersStates()
        setupPoinkuStates()

        updatePreview()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(STATE_CONFIG, config.toBundle())
        super.onSaveInstanceState(outState)
    }

    override fun onConfigMenuClicked() {
        showConfigSheet()
    }

    private fun setupCoreDefault() = with(binding) {
        chipCoreDefault.apply {
            text = "Core Chip"
            setStartIcon(UIKitR.drawable.ic_placeholder_medium_24)
            setEndIcon(UIKitR.drawable.ic_placeholder_medium_24)
        }
    }

    private fun setupKlikStates() = with(binding) {
        chipKlikDefault.setupTappable("Idle")
        chipKlikDefault.setStartIcon(UIKitR.drawable.ic_placeholder_medium_24)
        chipKlikDefault.setEndIcon(UIKitR.drawable.ic_placeholder_medium_24)

        chipKlikChecked.text = "Active"
        chipKlikChecked.setStartIcon(UIKitR.drawable.ic_placeholder_medium_24)
        chipKlikChecked.setEndIcon(UIKitR.drawable.ic_placeholder_medium_24)

        chipKlikInactive.text = "Selected"
        chipKlikInactive.setStartIcon(UIKitR.drawable.ic_placeholder_medium_24)
        chipKlikInactive.setEndIcon(UIKitR.drawable.ic_placeholder_medium_24)
    }

    private fun setupKlikFiltersStates() = with(binding) {
        chipKlikFiltersDefault.setupTappable("Idle")
        chipKlikFiltersDefault.setStartIcon(UIKitR.drawable.ic_placeholder_medium_24)
        chipKlikFiltersDefault.setEndIcon(UIKitR.drawable.ic_placeholder_medium_24)

        chipKlikFiltersChecked.text = "Active"
        chipKlikFiltersChecked.setStartIcon(UIKitR.drawable.ic_placeholder_medium_24)
        chipKlikFiltersChecked.setEndIcon(UIKitR.drawable.ic_placeholder_medium_24)

        chipKlikFiltersInactive.text = "Selected"
        chipKlikFiltersInactive.setStartIcon(UIKitR.drawable.ic_placeholder_medium_24)
        chipKlikFiltersInactive.setEndIcon(UIKitR.drawable.ic_placeholder_medium_24)
    }

    private fun setupPoinkuStates() = with(binding) {
        chipPoinkuDefault.setupTappable("Idle")
        chipPoinkuDefault.setStartIcon(UIKitR.drawable.ic_placeholder_medium_24)
        chipPoinkuDefault.setEndIcon(UIKitR.drawable.ic_placeholder_medium_24)

        chipPoinkuChecked.text = "Active"
        chipPoinkuChecked.setStartIcon(UIKitR.drawable.ic_placeholder_medium_24)
        chipPoinkuChecked.setEndIcon(UIKitR.drawable.ic_placeholder_medium_24)

        chipPoinkuInactive.text = "Selected"
        chipPoinkuInactive.setStartIcon(UIKitR.drawable.ic_placeholder_medium_24)
        chipPoinkuInactive.setEndIcon(UIKitR.drawable.ic_placeholder_medium_24)
    }

    private fun CoreChip.setupTappable(label: String) {
        text = label
        state = CHIP_STATE_IDLE
        delegate = object : ChipDelegate {
            override fun onClick(view: View, state: Int) {
                this@setupTappable.state =
                    if (state == CHIP_STATE_ACTIVE) CHIP_STATE_IDLE else CHIP_STATE_ACTIVE
            }
        }
    }

    private fun updatePreview() {
        val container = binding.previewContainer
        container.removeAllViews()

        val chip: CoreChip = when (config.product) {
            ChipProduct.CORE -> {
                CoreChip(this)
            }

            ChipProduct.KLIK -> {
                KlikChip(this).apply {
                    klikChipVariant = config.variant() as KlikChipVariant
                }
            }

            ChipProduct.POINKU -> {
                PoinkuChip(this)
            }
        }

        chip.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            gravity = Gravity.CENTER
        }

        chip.text = config.text
        chip.state = config.state

        config.tintColor?.let { chip.chipColor = it }
        config.textColor?.let { chip.chipTextColor = it }
        config.borderColor?.let { chip.chipBorderColor = it }
        chip.chipBorderWidth = config.borderWidth
        config.startIconColor?.let { chip.chipStartIconColor = it }
        config.endIconColor?.let { chip.chipEndIconColor = it }
        config.startIconColor?.let { chip.chipStartIconColor = it }
        config.startIconBackgroundColor?.let { chip.chipStartIconBackgroundColor = it }
        config.isStartIconBackgroundVisible?.let { chip.chipStartIconBackgroundVisible = it }
        config.endIconColor?.let { chip.chipEndIconColor = it }
        config.endIconBackgroundColor?.let { chip.chipEndIconBackgroundColor = it }
        config.isEndIconBackgroundVisible?.let { chip.chipEndIconBackgroundVisible = it }

        chip.setStartIcon(UIKitR.drawable.ic_placeholder_medium_24)
        chip.setEndIcon(UIKitR.drawable.ic_placeholder_medium_24)
        chip.isStartIconVisible = config.isStartIconVisible
        chip.isStartIconActionable = config.isStartIconActionable
        chip.isEndIconVisible = config.isEndIconVisible
        chip.isEndIconActionable = config.isEndIconActionable

        chip.delegate = object : ChipDelegate {
            override fun onClick(view: View, @ChipStateAnnotation state: Int) {
                toastPreview("Chip clicked")
            }

            override fun onStartIconClick(view: View, @ChipStateAnnotation state: Int) {
                toastPreview("Start icon clicked")
            }

            override fun onEndIconClick(view: View, @ChipStateAnnotation state: Int) {
                toastPreview("End icon clicked")
            }
        }

        container.addView(chip)
    }

    private fun toastPreview(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showConfigSheet() {
        if (supportFragmentManager.findFragmentByTag(ChipConfigBottomSheet.TAG) != null) return
        ChipConfigBottomSheet.newInstance(config).show(supportFragmentManager, ChipConfigBottomSheet.TAG)
    }

    companion object {
        private const val STATE_CONFIG = "chip_config_state"
    }
}