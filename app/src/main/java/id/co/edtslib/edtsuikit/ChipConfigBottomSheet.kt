package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import id.co.edtslib.edtsuikit.databinding.BottomSheetChipConfigBinding
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.R as UIKitR

class ChipConfigBottomSheet : BottomSheetTray() {
    private var config = ChipConfig()
    private var sheetBinding: BottomSheetChipConfigBinding? = null
    private var isUpdatingUi = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = savedInstanceState?.getBundle(STATE_CONFIG)?.toChipConfig() ?: arguments?.getBundle(ARG_CONFIG)?.toChipConfig() ?: ChipConfig()
        title = getString(R.string.configure_chip)
        dismissOnOutsideTouch = true
        isCancelableOnTouchOutside = true
        dragHandleVisibility = true
        titleDividerVisibility = true
        shouldShowClose = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        val binding = BottomSheetChipConfigBinding.inflate(inflater, null, false,)
        sheetBinding = binding
        contentView = binding.root

        return super.onCreateView(inflater, container, savedInstanceState,)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?, ) {
        super.onViewCreated(view, savedInstanceState)
        val localBinding = sheetBinding ?: return

        buildProductChips(localBinding.cgProduct)
        buildVariantChips(localBinding.cgVariant)
        buildStateChips(localBinding.cgState)

        localBinding.cgProduct.setOnCheckedStateChangeListener { group, ids ->
            if (isUpdatingUi) return@setOnCheckedStateChangeListener

            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId),)

            config = config.resetForVariant(
                product = ChipProduct.entries[index],
                variantIndex = 0,
            )

            refreshConfigUi(localBinding)
            publishConfig()
        }

        localBinding.cgVariant.setOnCheckedStateChangeListener { group, ids ->
            if (isUpdatingUi) return@setOnCheckedStateChangeListener

            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId),)

            config = config.resetForVariant(
                product = config.product,
                variantIndex = index,
            )

            refreshConfigUi(localBinding)
            publishConfig()
        }

        localBinding.cgState.setOnCheckedStateChangeListener { group, ids ->
            if (isUpdatingUi) return@setOnCheckedStateChangeListener

            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId),)
            config = config.copy(state = indexToState(index),)
            publishConfig()
        }

        localBinding.etText.doAfterTextChanged { text ->
            if (isUpdatingUi) return@doAfterTextChanged

            config = config.copy(text = text?.toString().orEmpty(),)

            publishConfig()
        }

        localBinding.colorPalette.onColorSelected = { color ->
            if (!isUpdatingUi) {
                config = config.copy(tintColor = color,)
                publishConfig()
            }
        }

        localBinding.colorPalette.onAddClicked = { context, selectedColor -> }
        localBinding.colorPaletteText.onColorSelected = { color ->
            if (!isUpdatingUi) {
                config = config.copy(textColor = color)
                publishConfig()
            }
        }

        localBinding.colorPaletteText.onAddClicked = { context, selectedColor -> }
        localBinding.colorPaletteBorder.onColorSelected = { color ->
            if (!isUpdatingUi) {
                config = config.copy(borderColor = color)
                publishConfig()
            }
        }

        localBinding.colorPaletteBorder.onAddClicked = { context, selectedColor -> }
        localBinding.colorPaletteStartIcon.onColorSelected = { color ->
            if (!isUpdatingUi) {
                config = config.copy(startIconColor = color)
                publishConfig()
            }
        }

        localBinding.colorPaletteStartIcon.onAddClicked = { context, selectedColor -> }
        localBinding.colorPaletteEndIcon.onColorSelected = { color ->
            if (!isUpdatingUi) {
                config = config.copy(endIconColor = color)
                publishConfig()
            }
        }

        localBinding.colorPaletteEndIcon.onAddClicked = { context, selectedColor -> }
        localBinding.slBorder.value = config.borderWidth.toFloat()
        localBinding.tvBorderValue.text = getString(R.string.border_width_value, config.borderWidth)
        localBinding.slBorder.addOnChangeListener { _, value, _ ->
            config = config.copy(borderWidth = value.toInt())
            localBinding.tvBorderValue.text = getString(R.string.border_width_value, value.toInt())
            publishConfig()
        }

        localBinding.colorPaletteStartIconBackground.onColorSelected = { color ->
            if (!isUpdatingUi) {
                config = config.copy(startIconBackgroundColor = color)
                publishConfig()
            }
        }

        localBinding.colorPaletteStartIconBackground.onAddClicked = { context, selectedColor -> }
        localBinding.colorPaletteEndIconBackground.onColorSelected = { color ->
            if (!isUpdatingUi) {
                config = config.copy(endIconBackgroundColor = color)
                publishConfig()
            }
        }

        localBinding.colorPaletteEndIconBackground.onAddClicked = { context, selectedColor -> }
        refreshConfigUi(localBinding)
    }

    private fun bindIconToggles(localBinding: BottomSheetChipConfigBinding) {
        localBinding.swStartIconVisible.isChecked = config.isStartIconVisible
        localBinding.swStartIconActionable.isChecked = config.isStartIconActionable
        localBinding.swStartIconBackgroundVisible.isChecked = config.isStartIconBackgroundVisible ?: false
        localBinding.startIconConfigContainer.isVisible = config.isStartIconVisible
        localBinding.startIconBackgroundConfigContainer.isVisible = config.isStartIconVisible && (config.isStartIconBackgroundVisible ?: false)
        localBinding.swEndIconVisible.isChecked = config.isEndIconVisible
        localBinding.swEndIconActionable.isChecked = config.isEndIconActionable
        localBinding.swEndIconBackgroundVisible.isChecked = config.isEndIconBackgroundVisible ?: false
        localBinding.endIconConfigContainer.isVisible = config.isEndIconVisible
        localBinding.endIconBackgroundConfigContainer.isVisible = config.isEndIconVisible && (config.isEndIconBackgroundVisible ?: false)

        localBinding.swStartIconVisible.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(
                isStartIconVisible = isChecked,
                isStartIconActionable = isChecked && config.isStartIconActionable,
                isStartIconBackgroundVisible = if (isChecked) config.isStartIconBackgroundVisible else false,
            )

            localBinding.startIconConfigContainer.isVisible = isChecked
            localBinding.startIconBackgroundConfigContainer.isVisible = isChecked && (config.isStartIconBackgroundVisible ?: false)
            localBinding.swStartIconActionable.isChecked = config.isStartIconActionable
            localBinding.swStartIconBackgroundVisible.isChecked = config.isStartIconBackgroundVisible ?: false
            publishConfig()
        }

        localBinding.swStartIconActionable.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(isStartIconActionable = isChecked)
            publishConfig()
        }

        localBinding.swStartIconBackgroundVisible.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(isStartIconBackgroundVisible = isChecked)
            localBinding.startIconBackgroundConfigContainer.isVisible = isChecked
            publishConfig()
        }

        localBinding.swEndIconVisible.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(
                isEndIconVisible = isChecked,
                isEndIconActionable = isChecked && config.isEndIconActionable,
                isEndIconBackgroundVisible = if (isChecked) config.isEndIconBackgroundVisible else false,
            )

            localBinding.endIconConfigContainer.isVisible = isChecked
            localBinding.endIconBackgroundConfigContainer.isVisible = isChecked && (config.isEndIconBackgroundVisible ?: false)
            localBinding.swEndIconActionable.isChecked = config.isEndIconActionable
            localBinding.swEndIconBackgroundVisible.isChecked = config.isEndIconBackgroundVisible ?: false
            publishConfig()
        }

        localBinding.swEndIconActionable.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(isEndIconActionable = isChecked)
            publishConfig()
        }

        localBinding.swEndIconBackgroundVisible.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(isEndIconBackgroundVisible = isChecked)
            localBinding.endIconBackgroundConfigContainer.isVisible = isChecked
            publishConfig()
        }
    }

    private fun refreshConfigUi(localBinding: BottomSheetChipConfigBinding, ) {
        isUpdatingUi = true

        try {
            buildProductChips(localBinding.cgProduct)
            buildVariantChips(localBinding.cgVariant)
            buildStateChips(localBinding.cgState)

            localBinding.etText.setText(config.text)
            localBinding.colorPalette.setColors(config.customColors, config.tintColor,)
            localBinding.colorPaletteText.setColors(config.customColors, config.textColor)
            localBinding.colorPaletteBorder.setColors(config.customColors, config.borderColor)
            localBinding.colorPaletteStartIcon.setColors(config.customColors, config.startIconColor)
            localBinding.colorPaletteStartIconBackground.setColors(config.customColors, config.startIconBackgroundColor)
            localBinding.colorPaletteEndIcon.setColors(config.customColors, config.endIconColor)
            localBinding.colorPaletteEndIconBackground.setColors(config.customColors, config.endIconBackgroundColor)

            if (localBinding.slBorder.value != config.borderWidth.toFloat()) {
                localBinding.slBorder.value = config.borderWidth.toFloat()
            }

            localBinding.tvBorderValue.text = getString(R.string.border_width_value, config.borderWidth)
            bindIconToggles(localBinding)
        } finally {
            isUpdatingUi = false
        }
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

    private fun buildProductChips(group: ChipGroup) = buildChips(group, ChipProduct.entries.map { it.label }, config.product.ordinal)

    private fun buildVariantChips(group: ChipGroup) {
        buildChips(group, config.product.variantLabels, config.variantIndex)
        group.isVisible = config.product == ChipProduct.KLIK
    }

    private fun buildStateChips(group: ChipGroup) = buildChips(group, config.stateLabels(), stateToIndex(config.state))

    private fun publishConfig() {
        parentFragmentManager.setFragmentResult(RESULT_CONFIG, config.toBundle())
    }

    companion object {
        const val TAG = "CHIP_CONFIG_SHEET"
        const val RESULT_CONFIG = "CHIP_CONFIG_RESULT"
        private const val ARG_CONFIG = "chip_config"
        private const val STATE_CONFIG = "chip_config_state"

        fun newInstance(config: ChipConfig): ChipConfigBottomSheet = ChipConfigBottomSheet().apply {
            arguments = Bundle().apply {
                putBundle(ARG_CONFIG, config.toBundle())
            }
        }
    }
}