package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import id.co.edtslib.edtsuikit.color.ColorPickerDialog
import id.co.edtslib.edtsuikit.color.ColorPaletteView
import id.co.edtslib.edtsuikit.databinding.BottomSheetButtonConfigBinding
import id.co.edtslib.uikit.R as UIKitR
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dimenPixelSize

class ButtonConfigBottomSheet : BottomSheetTray() {

    private var config = ButtonConfig()
    private var sheetBinding: BottomSheetButtonConfigBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = savedInstanceState?.getBundle(STATE_CONFIG)?.toButtonConfig()
            ?: arguments?.getBundle(ARG_CONFIG)?.toButtonConfig()
            ?: ButtonConfig()

        title = getString(R.string.configure_button)
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
        contentView = BottomSheetButtonConfigBinding.inflate(inflater, null, false).root
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val content = requireNotNull(binding.flContent.getChildAt(0))
        val localBinding = BottomSheetButtonConfigBinding.bind(content)
        sheetBinding = localBinding

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

        localBinding.swDestructive.isChecked = config.destructive
        localBinding.swDisabled.isChecked = config.disabled
        localBinding.swShimmer.isChecked = config.shimmer
        localBinding.slCornerRadius.value = config.cornerRadius
        localBinding.tvCornerRadiusValue.text = getString(
            R.string.corner_radius_value,
            config.cornerRadius.toInt(),
        )

        setupSizeFilters(localBinding)
        setupMutualExclusiveToggles(localBinding)

        localBinding.slCornerRadius.addOnChangeListener { _, value, _ ->
            config = config.copy(cornerRadius = value)
            localBinding.tvCornerRadiusValue.text = getString(R.string.corner_radius_value, value.toInt())
            publishConfig()
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

    private fun setupSizeFilters(binding: BottomSheetButtonConfigBinding) {
        val sizeFilters = listOf(
            R.string.size_filter_all,
            R.string.size_filter_small,
            R.string.size_filter_medium,
            R.string.size_filter_large,
        )
        val chipGroup = binding.cgSizeFilter
        chipGroup.removeAllViews()
        val context = chipGroup.context

        sizeFilters.forEachIndexed { index, labelRes ->
            val chip = Chip(context).apply {
                id = View.generateViewId()
                text = context.getString(labelRes)
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
                isChecked = when (config.sizeFilter) {
                    SizeFilter.ALL -> index == 0
                    SizeFilter.SMALL -> index == 1
                    SizeFilter.MEDIUM -> index == 2
                    SizeFilter.LARGE -> index == 3
                }
                contentDescription = context.getString(R.string.size_filter_description, text)
            }
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.firstOrNull()?.let { checkedId ->
                val index = chipGroup.indexOfChild(chipGroup.findViewById(checkedId))
                config = config.copy(
                    sizeFilter = when (index) {
                        0 -> SizeFilter.ALL
                        1 -> SizeFilter.SMALL
                        2 -> SizeFilter.MEDIUM
                        else -> SizeFilter.LARGE
                    },
                )
                publishConfig()
            }
        }
    }

    private fun setupMutualExclusiveToggles(binding: BottomSheetButtonConfigBinding) {
        binding.swDestructive.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(destructive = isChecked)
            if (isChecked) {
                binding.swDisabled.isChecked = false
                binding.swShimmer.isChecked = false
                config = config.copy(disabled = false, shimmer = false)
            }
            publishConfig()
        }

        binding.swDisabled.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(disabled = isChecked)
            if (isChecked) {
                binding.swDestructive.isChecked = false
                binding.swShimmer.isChecked = false
                config = config.copy(destructive = false, shimmer = false)
            }
            publishConfig()
        }

        binding.swShimmer.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(shimmer = isChecked)
            if (isChecked) {
                binding.swDestructive.isChecked = false
                binding.swDisabled.isChecked = false
                config = config.copy(destructive = false, disabled = false)
            }
            publishConfig()
        }
    }

    private fun publishConfig() {
        parentFragmentManager.setFragmentResult(RESULT_CONFIG, config.toBundle())
    }

    companion object {
        const val TAG = "BUTTON_CONFIG_SHEET"
        const val RESULT_CONFIG = "BUTTON_CONFIG_RESULT"

        private const val ARG_CONFIG = "button_config"
        private const val STATE_CONFIG = "button_config_state"

        fun newInstance(config: ButtonConfig): ButtonConfigBottomSheet =
            ButtonConfigBottomSheet().apply {
                arguments = Bundle().apply {
                    putBundle(ARG_CONFIG, config.toBundle())
                }
            }
    }
}
