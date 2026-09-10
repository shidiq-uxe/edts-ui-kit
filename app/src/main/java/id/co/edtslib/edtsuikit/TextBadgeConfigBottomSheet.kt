package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import id.co.edtslib.uikit.R as UIKitR
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import id.co.edtslib.edtsuikit.color.ColorPaletteView
import id.co.edtslib.edtsuikit.color.ColorPickerDialog
import id.co.edtslib.edtsuikit.databinding.BottomSheetTextBadgeConfigBinding
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dimenPixelSize

class TextBadgeConfigBottomSheet : BottomSheetTray() {

    private var config = TextBadgeConfig()
    private var sheetBinding: BottomSheetTextBadgeConfigBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = savedInstanceState?.getBundle(STATE_CONFIG)?.toTextBadgeConfig()
            ?: arguments?.getBundle(ARG_CONFIG)?.toTextBadgeConfig()
                    ?: TextBadgeConfig()

        title = getString(R.string.configure_text_badge)
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
        contentView = BottomSheetTextBadgeConfigBinding.inflate(inflater, null, false).root
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val content = requireNotNull(binding.flContent.getChildAt(0))
        val localBinding = BottomSheetTextBadgeConfigBinding.bind(content)
        sheetBinding = localBinding

        buildProductChips(localBinding.cgProduct)
        buildVariantChips(localBinding.cgVariant)
        buildSizeChips(localBinding.cgSize)

        localBinding.cgProduct.setOnCheckedStateChangeListener { group, ids ->
            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId))
            config = config.copy(
                product = TextBadgeProduct.entries[index],
                variantIndex = 0,
                sizeIndex = 0,
            )
            buildVariantChips(localBinding.cgVariant)
            buildSizeChips(localBinding.cgSize)
            publishConfig()
        }

        localBinding.cgVariant.setOnCheckedStateChangeListener { group, ids ->
            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId))
            config = config.copy(variantIndex = index, sizeIndex = 0)
            buildSizeChips(localBinding.cgSize)
            publishConfig()
        }

        localBinding.cgSize.setOnCheckedStateChangeListener { group, ids ->
            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId))
            config = config.copy(sizeIndex = index)
            publishConfig()
        }

        localBinding.etText.editText?.setText(config.text)
        localBinding.etText.editText?.doAfterTextChanged { editable ->
            config = config.copy(text = editable?.toString().orEmpty())
            publishConfig()
        }

        localBinding.swIcon.isChecked = config.iconVisible
        localBinding.swIcon.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(iconVisible = isChecked)
            publishConfig()
        }

        localBinding.swDisabled.isChecked = config.disabled
        localBinding.swDisabled.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(disabled = isChecked)
            publishConfig()
        }

        localBinding.slBorder.value = config.borderWidth.toFloat()
        localBinding.tvBorderValue.text = getString(R.string.border_width_value, config.borderWidth)
        localBinding.slBorder.addOnChangeListener { _, value, _ ->
            config = config.copy(borderWidth = value.toInt())
            localBinding.tvBorderValue.text = getString(R.string.border_width_value, value.toInt())
            publishConfig()
        }

        localBinding.slCorner.value = config.cornerRadius.toFloat()
        localBinding.tvCornerValue.text = getString(R.string.corner_radius_value, config.cornerRadius)
        localBinding.slCorner.addOnChangeListener { _, value, _ ->
            config = config.copy(cornerRadius = value.toInt())
            localBinding.tvCornerValue.text = getString(R.string.corner_radius_value, value.toInt())
            publishConfig()
        }

        localBinding.colorPalette.setColors(config.customColors, config.tintColor)
        localBinding.colorPalette.onColorSelected = { color ->
            config = config.copy(tintColor = color)
            publishConfig()
        }
        localBinding.colorPalette.onAddClicked = { context, selectedColor ->
            ColorPickerDialog.show(
                context = context,
                title = getString(R.string.add_color),
                selectedColor = selectedColor,
                onColorPicked = { color ->
                    val newColors = if (config.customColors.contains(color)) {
                        config.customColors
                    } else {
                        (listOf(color) + config.customColors)
                            .take(ColorPaletteView.MAX_VISIBLE_COLORS)
                    }
                    config = config.copy(tintColor = color, customColors = newColors)
                    publishConfig()
                    sheetBinding?.colorPalette?.setColors(newColors, color)
                },
            )
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

    private fun buildProductChips(group: ChipGroup) =
        buildChips(group, TextBadgeProduct.entries.map { it.label }, config.product.ordinal)

    private fun buildVariantChips(group: ChipGroup) =
        buildChips(
            group,
            config.product.variantLabels,
            config.variantIndex.coerceIn(0, config.product.variantLabels.lastIndex),
        )

    private fun buildSizeChips(group: ChipGroup) {
        val sizes = config.sizeLabels()
        buildChips(group, sizes, config.sizeIndex.coerceIn(0, sizes.lastIndex))
    }

    private fun publishConfig() {
        parentFragmentManager.setFragmentResult(RESULT_CONFIG, config.toBundle())
    }

    companion object {
        const val TAG = "TEXT_BADGE_CONFIG_SHEET"
        const val RESULT_CONFIG = "TEXT_BADGE_CONFIG_RESULT"

        private const val ARG_CONFIG = "text_badge_config"
        private const val STATE_CONFIG = "text_badge_config_state"

        fun newInstance(config: TextBadgeConfig): TextBadgeConfigBottomSheet =
            TextBadgeConfigBottomSheet().apply {
                arguments = Bundle().apply {
                    putBundle(ARG_CONFIG, config.toBundle())
                }
            }
    }
}