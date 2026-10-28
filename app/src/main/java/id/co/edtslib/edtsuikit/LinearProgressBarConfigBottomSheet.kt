package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import id.co.edtslib.edtsuikit.color.ColorPaletteView
import id.co.edtslib.edtsuikit.color.ColorPickerDialog
import id.co.edtslib.edtsuikit.databinding.BottomSheetProgressBarConfigBinding
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dimenPixelSize
import kotlin.math.roundToInt
import id.co.edtslib.uikit.R as UIKitR

class LinearProgressBarConfigBottomSheet : BottomSheetTray() {

    private var config = ProgressBarConfig()
    private var sheetBinding: BottomSheetProgressBarConfigBinding? = null
    private var isBindingUi = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = savedInstanceState?.getBundle(STATE_CONFIG)?.toProgressBarConfig()
            ?: arguments?.getBundle(ARG_CONFIG)?.toProgressBarConfig()
                    ?: ProgressBarConfig()

        title = getString(R.string.configure_progress_bar)
        dismissOnOutsideTouch = true
        isCancelableOnTouchOutside = true
        dragHandleVisibility = true
        titleDividerVisibility = true
        shouldShowClose = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        contentView = BottomSheetProgressBarConfigBinding.inflate(inflater, null, false).root
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val content = requireNotNull(binding.flContent.getChildAt(0))
        val b = BottomSheetProgressBarConfigBinding.bind(content)
        sheetBinding = b

        buildChips(b.cgProduct, ProgressBarProduct.entries.map { it.label }, config.product.ordinal)
        buildChips(b.cgMode, ProgressBarMode.entries.map { it.label }, config.mode.ordinal)
        bindControls(b)

        b.cgProduct.setOnCheckedStateChangeListener { group, ids ->
            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId))
            config = config.withProduct(ProgressBarProduct.entries[index])
            bindControls(b)
            publishConfig()
        }

        b.cgMode.setOnCheckedStateChangeListener { group, ids ->
            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId))
            config = config.withMode(ProgressBarMode.entries[index])
            bindControls(b)
            publishConfig()
        }

        b.slProgress.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            config = config.copy(progress = value.toInt())
            b.tvProgressValue.text = getString(R.string.progress_percent_value, value.toInt())
            publishConfig()
        }
        b.slThickness.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            config = config.copy(thicknessDp = value.toInt())
            b.tvThicknessValue.text = getString(R.string.track_thickness_value, value.toInt())
            publishConfig()
        }

        b.swBadge.setOnCheckedChangeListener { _, isChecked ->
            if (isBindingUi) return@setOnCheckedChangeListener
            config = config.copy(showBadge = isChecked)
            publishConfig()
        }

        b.swDisabled.setOnCheckedChangeListener { _, isChecked ->
            if (isBindingUi) return@setOnCheckedChangeListener
            config = config.copy(disabled = isChecked)
            publishConfig()
        }

        b.swThumb.setOnCheckedChangeListener { _, isChecked ->
            if (isBindingUi) return@setOnCheckedChangeListener
            config = config.copy(showThumb = isChecked)
            b.groupThumb.isVisible = isChecked
            publishConfig()
        }

        b.slThumbSize.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            config = config.copy(thumbSizeDp = value.toInt())
            b.tvThumbSizeValue.text = getString(R.string.thumb_size_value, value.toInt())
            publishConfig()
        }

        b.swShadow.setOnCheckedChangeListener { _, isChecked ->
            if (isBindingUi) return@setOnCheckedChangeListener
            config = config.copy(showInnerShadow = isChecked)
            b.groupShadow.isVisible = isChecked
            publishConfig()
        }
        b.slShadowX.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            config = config.copy(shadowOffsetX = value)
            b.tvShadowXValue.text = getString(R.string.shadow_value, value)
            publishConfig()
        }
        b.slShadowY.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            config = config.copy(shadowOffsetY = value)
            b.tvShadowYValue.text = getString(R.string.shadow_value, value)
            publishConfig()
        }
        b.slShadowSpread.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            config = config.copy(shadowSpread = value)
            b.tvShadowSpreadValue.text = getString(R.string.shadow_value, value)
            publishConfig()
        }

        setupPalette(b.paletteIndicator) { config = config.copy(indicatorColor = it) }
        setupPalette(b.paletteThumb) { config = config.copy(thumbColor = it) }
        setupPalette(b.paletteTrack) { config = config.copy(trackColor = it) }
        setupPalette(b.paletteCompleted) { config = config.copy(completedColor = it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(STATE_CONFIG, config.toBundle())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        sheetBinding = null
        contentView = null
        super.onDestroyView()
    }

    private fun bindControls(b: BottomSheetProgressBarConfigBinding) {
        val defaults = config.product.create(requireContext())
        val density = resources.displayMetrics.density

        isBindingUi = true

        b.slProgress.value = config.progress.toFloat().coerceIn(PROGRESS_MIN, PROGRESS_MAX)
        b.slProgress.isEnabled = config.mode.usesProgress
        b.tvProgressValue.text = getString(R.string.progress_percent_value, config.progress)

        val thickness = (config.thicknessDp ?: (defaults.trackThickness / density).roundToInt())
            .coerceIn(THICKNESS_MIN, THICKNESS_MAX)
        b.slThickness.value = thickness.toFloat()
        b.tvThicknessValue.text = getString(R.string.track_thickness_value, thickness)
        b.swBadge.isChecked = config.showBadge ?: (config.mode == ProgressBarMode.MULTI_LAP || defaults.showBadge)
        b.swDisabled.isChecked = config.disabled

        val showThumb = config.showThumb ?: defaults.showThumb
        b.swThumb.isChecked = showThumb
        b.groupThumb.isVisible = showThumb

        val thumbSize = (config.thumbSizeDp ?: (defaults.thumbSize / density).roundToInt())
            .coerceIn(THUMB_SIZE_MIN, THUMB_SIZE_MAX)
        b.slThumbSize.value = thumbSize.toFloat()
        b.tvThumbSizeValue.text = getString(R.string.thumb_size_value, thumbSize)

        val showShadow = config.showInnerShadow ?: defaults.showInnerShadow
        b.swShadow.isChecked = showShadow
        b.groupShadow.isVisible = showShadow
        bindShadowSlider(b.slShadowX, b.tvShadowXValue, config.shadowOffsetX ?: (defaults.innerShadowOffsetX / density), SHADOW_OFFSET_MIN)
        bindShadowSlider(b.slShadowY, b.tvShadowYValue, config.shadowOffsetY ?: (defaults.innerShadowOffsetY / density), SHADOW_OFFSET_MIN)
        bindShadowSlider(b.slShadowSpread, b.tvShadowSpreadValue, config.shadowSpread ?: (defaults.innerShadowSpread / density), SHADOW_SPREAD_MIN)
        b.groupCompleted.isVisible = config.mode == ProgressBarMode.MULTI_LAP

        refreshPalettes(b)
        isBindingUi = false
    }

    private fun bindShadowSlider(slider: Slider, label: TextView, valueDp: Float, min: Float) {
        val snapped = ((valueDp * 2f).roundToInt() / 2f).coerceIn(min, SHADOW_MAX)
        slider.value = snapped
        label.text = getString(R.string.shadow_value, snapped)
    }

    private fun refreshPalettes(b: BottomSheetProgressBarConfigBinding) {
        b.paletteIndicator.setColors(config.customColors, config.indicatorColor)
        b.paletteThumb.setColors(config.customColors, config.thumbColor)
        b.paletteTrack.setColors(config.customColors, config.trackColor)
        b.paletteCompleted.setColors(config.customColors, config.completedColor)
    }

    private fun setupPalette(palette: ColorPaletteView, onPicked: (Int) -> Unit) {
        palette.onColorSelected = { color ->
            onPicked(color)
            publishConfig()
        }
        palette.onAddClicked = { context, selectedColor ->
            ColorPickerDialog.show(
                context = context,
                title = getString(R.string.add_color),
                selectedColor = selectedColor,
                onColorPicked = { color ->
                    onPicked(color)
                    if (!config.customColors.contains(color)) {
                        config = config.copy(
                            customColors = (listOf(color) + config.customColors)
                                .take(ColorPaletteView.MAX_VISIBLE_COLORS),
                        )
                    }
                    publishConfig()
                    sheetBinding?.let { refreshPalettes(it) }
                },
            )
        }
    }

    private fun buildChips(group: ChipGroup, labels: List<String>, checkedIndex: Int) {
        group.removeAllViews()
        val context = group.context

        labels.forEachIndexed { index, label ->
            val chip = Chip(context).apply {
                id = View.generateViewId()
                text = label
                isCheckable = true
                setTextAppearance(UIKitR.style.TextAppearance_Inter_Regular_B2)
                chipBackgroundColor = context.colorStateList(UIKitR.color.slr_chip_filter_bg)
                setTextColor(context.colorStateList(UIKitR.color.slr_chip_filter_text))
                chipStrokeColor = context.colorStateList(UIKitR.color.black_30)
                chipStrokeWidth = context.dimenPixelSize(UIKitR.dimen.dimen_1)?.toFloat() ?: 0f
                chipCornerRadius = context.dimen(UIKitR.dimen.xs)
                chipStartPadding = context.dimenPixelSize(UIKitR.dimen.s)?.toFloat() ?: 0f
                chipEndPadding = context.dimenPixelSize(UIKitR.dimen.s)?.toFloat() ?: 0f
                chipMinHeight = context.dimenPixelSize(UIKitR.dimen.dimen_32)?.toFloat() ?: 0f
                isChecked = index == checkedIndex
            }
            group.addView(chip)
        }
    }

    private fun publishConfig() {
        parentFragmentManager.setFragmentResult(RESULT_CONFIG, config.toBundle())
    }

    companion object {
        const val TAG = "PROGRESS_BAR_CONFIG_SHEET"
        const val RESULT_CONFIG = "PROGRESS_BAR_CONFIG_RESULT"

        private const val ARG_CONFIG = "progress_bar_config"
        private const val STATE_CONFIG = "progress_bar_config_state"

        private const val PROGRESS_MIN = 0f
        private const val PROGRESS_MAX = 100f
        private const val THICKNESS_MIN = 1
        private const val THICKNESS_MAX = 16
        private const val THUMB_SIZE_MIN = 2
        private const val THUMB_SIZE_MAX = 32
        private const val SHADOW_OFFSET_MIN = -8f
        private const val SHADOW_SPREAD_MIN = 0f
        private const val SHADOW_MAX = 8f

        fun newInstance(config: ProgressBarConfig): LinearProgressBarConfigBottomSheet =
            LinearProgressBarConfigBottomSheet().apply {
                arguments = Bundle().apply {
                    putBundle(ARG_CONFIG, config.toBundle())
                }
            }
    }
}