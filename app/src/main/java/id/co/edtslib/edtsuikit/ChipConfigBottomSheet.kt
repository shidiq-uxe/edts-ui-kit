package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import id.co.edtslib.edtsuikit.databinding.BottomSheetChipConfigBinding
import id.co.edtslib.edtsuikit.databinding.BottomSheetChipGroupConfigBinding
import id.co.edtslib.edtsuikit.databinding.BottomSheetChipsConfigBinding
import id.co.edtslib.uikit.core.chipgroup.ChipGroup.ChipGroupOrientation
import id.co.edtslib.uikit.databinding.HomepageSwitcherBinding
import id.co.edtslib.uikit.switcher.HomeSwitcher
import id.co.edtslib.uikit.switcher.HomeSwitcherDelegate
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.R as UIKitR

class ChipConfigBottomSheet : BottomSheetTray() {
    private var chipConfig = ChipConfig()
    private var groupConfig = ChipGroupConfig()
    private var selectedPane = PANE_CHIP
    private var sheetBinding: BottomSheetChipsConfigBinding? = null
    private var isUpdatingUi = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chipConfig = savedInstanceState?.getBundle(STATE_CHIP_CONFIG)?.toChipConfig() ?: arguments?.getBundle(ARG_CHIP_CONFIG)?.toChipConfig() ?: ChipConfig()
        groupConfig = savedInstanceState?.getBundle(STATE_GROUP_CONFIG)?.toChipGroupConfig() ?: arguments?.getBundle(ARG_GROUP_CONFIG)?.toChipGroupConfig() ?: ChipGroupConfig()
        selectedPane = savedInstanceState?.getInt(STATE_PANE) ?: arguments?.getInt(ARG_PANE) ?: PANE_CHIP

        title = getString(R.string.configure_chip)
        dismissOnOutsideTouch = true
        isCancelableOnTouchOutside = true
        dragHandleVisibility = true
        titleDividerVisibility = true
        shouldShowClose = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = BottomSheetChipsConfigBinding.inflate(inflater, null, false)
        sheetBinding = binding
        contentView = binding.root

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = sheetBinding ?: return

        bindChipPane(binding.paneChip)
        bindGroupPane(binding.paneChipGroup)
        bindSwitcher(binding)
    }

    private fun bindSwitcher(binding: BottomSheetChipsConfigBinding) {
        val switcherViews = HomepageSwitcherBinding.bind(binding.switcher.getChildAt(0))

        hideSwitcherIconsAndSubtitles(switcherViews)
        addSwitcherTitleBottomPadding(switcherViews, 8)
        setSwitcherActiveColor(switcherViews)

        binding.switcher.delegate = object : HomeSwitcherDelegate {
            override fun onSwitchChangedListener(selectedTab: HomeSwitcher.Tab) {
                showPane(binding, if (selectedTab == HomeSwitcher.Tab.Xtra) PANE_GROUP else PANE_CHIP)
            }

            override fun onSwitchAnimationEndListener(selectedTab: HomeSwitcher.Tab) = Unit
        }

        showPane(binding, selectedPane)

        binding.switcher.doOnLayout {
            binding.switcher.shouldAnimate = false
            binding.switcher.selectedTab =
                if (selectedPane == PANE_GROUP) HomeSwitcher.Tab.Xtra else HomeSwitcher.Tab.Xpress
            binding.switcher.shouldAnimate = true
        }
    }

    private fun hideSwitcherIconsAndSubtitles(views: HomepageSwitcherBinding) {
        listOf(views.ivXpressLogo, views.ivXtraLogo, views.tvXpressSubtitle, views.tvXtraSubtitle)
            .forEach { it.isVisible = false }
    }

    private fun addSwitcherTitleBottomPadding(views: HomepageSwitcherBinding, dp: Int) {
        val px = (dp * resources.displayMetrics.density).toInt()
        listOf(views.tvXpressTitle, views.tvXtraTitle).forEach { it.updatePadding(bottom = px) }
    }

    private fun setSwitcherActiveColor(views: HomepageSwitcherBinding) {
        views.activeTab.backgroundTintList =
            requireContext().colorStateList(UIKitR.color.foundation_color_primary_blue_50)
    }

    private fun showPane(binding: BottomSheetChipsConfigBinding, pane: Int) {
        selectedPane = pane
        binding.paneChip.root.isVisible = pane == PANE_CHIP
        binding.paneChipGroup.root.isVisible = pane == PANE_GROUP
    }

    private fun bindChipPane(pane: BottomSheetChipConfigBinding) {
        buildProductChips(pane.cgProduct)
        buildVariantChips(pane.cgVariant)
        buildStateChips(pane.cgState)

        pane.cgProduct.setOnCheckedStateChangeListener { group, ids ->
            if (isUpdatingUi) return@setOnCheckedStateChangeListener

            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId))

            chipConfig = chipConfig.resetForVariant(
                product = ChipProduct.entries[index],
                variantIndex = 0,
            )

            refreshChipUi(pane)
            publishChipConfig()
        }

        pane.cgVariant.setOnCheckedStateChangeListener { group, ids ->
            if (isUpdatingUi) return@setOnCheckedStateChangeListener

            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId))

            chipConfig = chipConfig.resetForVariant(
                product = chipConfig.product,
                variantIndex = index,
            )

            refreshChipUi(pane)
            publishChipConfig()
        }

        pane.cgState.setOnCheckedStateChangeListener { group, ids ->
            if (isUpdatingUi) return@setOnCheckedStateChangeListener

            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId))
            chipConfig = chipConfig.copy(state = indexToState(index))
            publishChipConfig()
        }

        pane.etText.doAfterTextChanged { text ->
            if (isUpdatingUi) return@doAfterTextChanged

            chipConfig = chipConfig.copy(text = text?.toString().orEmpty())
            publishChipConfig()
        }

        pane.colorPalette.onColorSelected = { color ->
            if (!isUpdatingUi) {
                chipConfig = chipConfig.copy(tintColor = color)
                publishChipConfig()
            }
        }
        pane.colorPalette.onAddClicked = { context, selectedColor -> }

        pane.colorPaletteText.onColorSelected = { color ->
            if (!isUpdatingUi) {
                chipConfig = chipConfig.copy(textColor = color)
                publishChipConfig()
            }
        }
        pane.colorPaletteText.onAddClicked = { context, selectedColor -> }

        pane.colorPaletteBorder.onColorSelected = { color ->
            if (!isUpdatingUi) {
                chipConfig = chipConfig.copy(borderColor = color)
                publishChipConfig()
            }
        }
        pane.colorPaletteBorder.onAddClicked = { context, selectedColor -> }

        pane.colorPaletteStartIcon.onColorSelected = { color ->
            if (!isUpdatingUi) {
                chipConfig = chipConfig.copy(startIconColor = color)
                publishChipConfig()
            }
        }
        pane.colorPaletteStartIcon.onAddClicked = { context, selectedColor -> }

        pane.colorPaletteEndIcon.onColorSelected = { color ->
            if (!isUpdatingUi) {
                chipConfig = chipConfig.copy(endIconColor = color)
                publishChipConfig()
            }
        }
        pane.colorPaletteEndIcon.onAddClicked = { context, selectedColor -> }

        pane.slBorder.value = chipConfig.borderWidth.toFloat()
        pane.tvBorderValue.text = getString(R.string.border_width_value, chipConfig.borderWidth)
        pane.slBorder.addOnChangeListener { _, value, _ ->
            chipConfig = chipConfig.copy(borderWidth = value.toInt())
            pane.tvBorderValue.text = getString(R.string.border_width_value, value.toInt())
            publishChipConfig()
        }

        pane.colorPaletteStartIconBackground.onColorSelected = { color ->
            if (!isUpdatingUi) {
                chipConfig = chipConfig.copy(startIconBackgroundColor = color)
                publishChipConfig()
            }
        }
        pane.colorPaletteStartIconBackground.onAddClicked = { context, selectedColor -> }

        pane.colorPaletteEndIconBackground.onColorSelected = { color ->
            if (!isUpdatingUi) {
                chipConfig = chipConfig.copy(endIconBackgroundColor = color)
                publishChipConfig()
            }
        }
        pane.colorPaletteEndIconBackground.onAddClicked = { context, selectedColor -> }

        refreshChipUi(pane)
    }

    private fun bindIconToggles(pane: BottomSheetChipConfigBinding) {
        pane.swStartIconVisible.isChecked = chipConfig.isStartIconVisible
        pane.swStartIconActionable.isChecked = chipConfig.isStartIconActionable
        pane.swStartIconBackgroundVisible.isChecked = chipConfig.isStartIconBackgroundVisible ?: false
        pane.startIconConfigContainer.isVisible = chipConfig.isStartIconVisible
        pane.startIconBackgroundConfigContainer.isVisible = chipConfig.isStartIconVisible && (chipConfig.isStartIconBackgroundVisible ?: false)
        pane.swEndIconVisible.isChecked = chipConfig.isEndIconVisible
        pane.swEndIconActionable.isChecked = chipConfig.isEndIconActionable
        pane.swEndIconBackgroundVisible.isChecked = chipConfig.isEndIconBackgroundVisible ?: false
        pane.endIconConfigContainer.isVisible = chipConfig.isEndIconVisible
        pane.endIconBackgroundConfigContainer.isVisible = chipConfig.isEndIconVisible && (chipConfig.isEndIconBackgroundVisible ?: false)

        pane.swStartIconVisible.setOnCheckedChangeListener { _, isChecked ->
            chipConfig = chipConfig.copy(
                isStartIconVisible = isChecked,
                isStartIconActionable = isChecked && chipConfig.isStartIconActionable,
                isStartIconBackgroundVisible = if (isChecked) chipConfig.isStartIconBackgroundVisible else false,
            )

            pane.startIconConfigContainer.isVisible = isChecked
            pane.startIconBackgroundConfigContainer.isVisible = isChecked && (chipConfig.isStartIconBackgroundVisible ?: false)
            pane.swStartIconActionable.isChecked = chipConfig.isStartIconActionable
            pane.swStartIconBackgroundVisible.isChecked = chipConfig.isStartIconBackgroundVisible ?: false
            publishChipConfig()
        }

        pane.swStartIconActionable.setOnCheckedChangeListener { _, isChecked ->
            chipConfig = chipConfig.copy(isStartIconActionable = isChecked)
            publishChipConfig()
        }

        pane.swStartIconBackgroundVisible.setOnCheckedChangeListener { _, isChecked ->
            chipConfig = chipConfig.copy(isStartIconBackgroundVisible = isChecked)
            pane.startIconBackgroundConfigContainer.isVisible = isChecked
            publishChipConfig()
        }

        pane.swEndIconVisible.setOnCheckedChangeListener { _, isChecked ->
            chipConfig = chipConfig.copy(
                isEndIconVisible = isChecked,
                isEndIconActionable = isChecked && chipConfig.isEndIconActionable,
                isEndIconBackgroundVisible = if (isChecked) chipConfig.isEndIconBackgroundVisible else false,
            )

            pane.endIconConfigContainer.isVisible = isChecked
            pane.endIconBackgroundConfigContainer.isVisible = isChecked && (chipConfig.isEndIconBackgroundVisible ?: false)
            pane.swEndIconActionable.isChecked = chipConfig.isEndIconActionable
            pane.swEndIconBackgroundVisible.isChecked = chipConfig.isEndIconBackgroundVisible ?: false
            publishChipConfig()
        }

        pane.swEndIconActionable.setOnCheckedChangeListener { _, isChecked ->
            chipConfig = chipConfig.copy(isEndIconActionable = isChecked)
            publishChipConfig()
        }

        pane.swEndIconBackgroundVisible.setOnCheckedChangeListener { _, isChecked ->
            chipConfig = chipConfig.copy(isEndIconBackgroundVisible = isChecked)
            pane.endIconBackgroundConfigContainer.isVisible = isChecked
            publishChipConfig()
        }
    }

    private fun refreshChipUi(pane: BottomSheetChipConfigBinding) {
        isUpdatingUi = true

        try {
            buildProductChips(pane.cgProduct)
            buildVariantChips(pane.cgVariant)
            buildStateChips(pane.cgState)

            pane.etText.setText(chipConfig.text)
            pane.colorPalette.setColors(chipConfig.customColors, chipConfig.tintColor)
            pane.colorPaletteText.setColors(chipConfig.customColors, chipConfig.textColor)
            pane.colorPaletteBorder.setColors(chipConfig.customColors, chipConfig.borderColor)
            pane.colorPaletteStartIcon.setColors(chipConfig.customColors, chipConfig.startIconColor)
            pane.colorPaletteStartIconBackground.setColors(chipConfig.customColors, chipConfig.startIconBackgroundColor)
            pane.colorPaletteEndIcon.setColors(chipConfig.customColors, chipConfig.endIconColor)
            pane.colorPaletteEndIconBackground.setColors(chipConfig.customColors, chipConfig.endIconBackgroundColor)

            if (pane.slBorder.value != chipConfig.borderWidth.toFloat()) {
                pane.slBorder.value = chipConfig.borderWidth.toFloat()
            }

            pane.tvBorderValue.text = getString(R.string.border_width_value, chipConfig.borderWidth)
            bindIconToggles(pane)
        } finally {
            isUpdatingUi = false
        }
    }

    private fun buildProductChips(group: ChipGroup) =
        buildChips(group, ChipProduct.entries.map { it.label }, chipConfig.product.ordinal)

    private fun buildVariantChips(group: ChipGroup) {
        buildChips(group, chipConfig.product.variantLabels, chipConfig.variantIndex)
        group.isVisible = chipConfig.product == ChipProduct.KLIK
    }

    private fun buildStateChips(group: ChipGroup) =
        buildChips(group, chipConfig.stateLabels(), stateToIndex(chipConfig.state))

    private fun publishChipConfig() {
        parentFragmentManager.setFragmentResult(RESULT_CONFIG, chipConfig.toBundle())
    }

    private fun bindGroupPane(pane: BottomSheetChipGroupConfigBinding) {
        buildOrientationChips(pane.cgOrientation)

        pane.cgOrientation.setOnCheckedStateChangeListener { group, ids ->
            if (isUpdatingUi) return@setOnCheckedStateChangeListener

            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId))

            groupConfig = groupConfig.copy(orientation = indexToOrientation(index))
            refreshGroupUi(pane)
            publishGroupConfig()
        }

        buildSelectionModeChips(pane.cgSelectionMode)

        pane.cgSelectionMode.setOnCheckedStateChangeListener { group, ids ->
            if (isUpdatingUi) return@setOnCheckedStateChangeListener

            val checkedId = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val index = group.indexOfChild(group.findViewById(checkedId))

            groupConfig = groupConfig.copy(selectionMode = indexToSelectionMode(index))
            publishGroupConfig()
        }

        pane.slSpacingHorizontal.value = groupConfig.itemSpacingHorizontalDp.toFloat()
        pane.tvSpacingHorizontalValue.text = getString(R.string.spacing_value, groupConfig.itemSpacingHorizontalDp)
        pane.slSpacingHorizontal.addOnChangeListener { _, value, _ ->
            groupConfig = groupConfig.copy(itemSpacingHorizontalDp = value.toInt())
            pane.tvSpacingHorizontalValue.text = getString(R.string.spacing_value, value.toInt())
            publishGroupConfig()
        }

        pane.slSpacingVertical.value = groupConfig.itemSpacingVerticalDp.toFloat()
        pane.tvSpacingVerticalValue.text = getString(R.string.spacing_value, groupConfig.itemSpacingVerticalDp)
        pane.slSpacingVertical.addOnChangeListener { _, value, _ ->
            groupConfig = groupConfig.copy(itemSpacingVerticalDp = value.toInt())
            pane.tvSpacingVerticalValue.text = getString(R.string.spacing_value, value.toInt())
            publishGroupConfig()
        }

        pane.slSpanCount.value = (groupConfig.spanCount ?: 0).toFloat()
        pane.tvSpanCountValue.text = spanCountText(groupConfig.spanCount)
        pane.slSpanCount.addOnChangeListener { _, value, _ ->
            val span = value.toInt().takeIf { it > 0 }
            groupConfig = groupConfig.copy(spanCount = span)
            pane.tvSpanCountValue.text = spanCountText(span)
            publishGroupConfig()
        }

        pane.slItemCount.value = groupConfig.itemCount.toFloat()
        pane.tvItemCountValue.text = groupConfig.itemCount.toString()
        pane.slItemCount.addOnChangeListener { _, value, _ ->
            groupConfig = groupConfig.copy(itemCount = value.toInt())
            pane.tvItemCountValue.text = value.toInt().toString()
            publishGroupConfig()
        }

        refreshGroupUi(pane)
    }

    private fun spanCountText(span: Int?): String = span?.toString() ?: "Auto (wrap)"

    private fun refreshGroupUi(pane: BottomSheetChipGroupConfigBinding) {
        isUpdatingUi = true

        try {
            buildOrientationChips(pane.cgOrientation)
            buildSelectionModeChips(pane.cgSelectionMode)

            pane.spanCountContainer.isVisible = groupConfig.orientation == ChipGroupOrientation.GRID
        } finally {
            isUpdatingUi = false
        }
    }

    private fun buildOrientationChips(group: ChipGroup) =
        buildChips(group, groupConfig.orientationLabels(), orientationToIndex(groupConfig.orientation))

    private fun buildSelectionModeChips(group: ChipGroup) =
        buildChips(group, groupConfig.selectionModeLabels(), selectionModeToIndex(groupConfig.selectionMode))

    private fun publishGroupConfig() {
        parentFragmentManager.setFragmentResult(RESULT_CHIP_GROUP_CONFIG, groupConfig.toBundle())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(STATE_CHIP_CONFIG, chipConfig.toBundle())
        outState.putBundle(STATE_GROUP_CONFIG, groupConfig.toBundle())
        outState.putInt(STATE_PANE, selectedPane)
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

    companion object {
        const val TAG = "CHIP_CONFIG_SHEET"
        const val RESULT_CONFIG = "CHIP_CONFIG_RESULT"
        const val RESULT_CHIP_GROUP_CONFIG = "CHIP_GROUP_CONFIG_RESULT"

        const val PANE_CHIP = 0
        const val PANE_GROUP = 1

        private const val ARG_CHIP_CONFIG = "chip_config"
        private const val ARG_GROUP_CONFIG = "chip_group_config"
        private const val ARG_PANE = "pane"
        private const val STATE_CHIP_CONFIG = "chip_config_state"
        private const val STATE_GROUP_CONFIG = "chip_group_config_state"
        private const val STATE_PANE = "pane_state"

        fun newInstance(
            config: ChipConfig,
            chipGroupConfig: ChipGroupConfig,
            initialPane: Int = PANE_CHIP,
        ): ChipConfigBottomSheet = ChipConfigBottomSheet().apply {
            arguments = Bundle().apply {
                putBundle(ARG_CHIP_CONFIG, config.toBundle())
                putBundle(ARG_GROUP_CONFIG, chipGroupConfig.toBundle())
                putInt(ARG_PANE, initialPane)
            }
        }
    }
}