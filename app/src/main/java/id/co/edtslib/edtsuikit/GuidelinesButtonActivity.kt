package id.co.edtslib.edtsuikit

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import id.co.edtslib.edtsuikit.color.ColorPaletteSheet
import id.co.edtslib.edtsuikit.databinding.ActivityButtonBinding
import id.co.edtslib.edtsuikit.databinding.BottomSheetButtonConfigBinding
import id.co.edtslib.edtsuikit.helper.GuidelineConfigSheet
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.adapter.ViewAdapter
import id.co.edtslib.uikit.button.Button
import id.co.edtslib.uikit.button.Button.ButtonType
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesButtonActivity : GuidelinesBaseActivity() {

    override val hasConfigMenu = true

    private val binding by viewBinding<ActivityButtonBinding>()

    override val enableEdgeToEdge: Boolean
        get() = false

    private var buttonAdapter: ViewAdapter<Button, ButtonVariant>? = null

    private var config = ButtonConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_button)
        setupButtonList()
    }

    override fun onConfigMenuClicked() {
        showConfigSheet()
    }

    private fun setupButtonList() {
        binding.rvButtons.layoutManager = LinearLayoutManager(this)

        buttonAdapter = ViewAdapter.adapterOf<Button, ButtonVariant>(
            viewFactory = { context -> createButton(context) },
            diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old.section == new.section && old.type == new.type && old.size == new.size },
                areContentsTheSame = { _, _ -> false }
            ),
            configure = { position, button, variant ->
                button.strokeWidth = 0
                button.strokeColor = null

                button.buttonType = variant.type
                button.text = variant.label
                button.isEnabled = !config.disabled
                button.isDestructive = config.destructive
                button.shouldShowShimmer = config.shimmer
                button.cornerRadius = config.cornerRadius.toInt()

                if (!config.destructive && !config.disabled) {
                    config.tintColor?.let { color ->
                        val colorList = ColorStateList.valueOf(color)
                        when (variant.section) {
                            Section.FILLED -> button.backgroundTintList = colorList
                            Section.SECONDARY, Section.TERTIARY -> {
                                button.strokeColor = colorList
                                button.setTextColor(color)
                            }
                            Section.TEXT -> button.setTextColor(color)
                        }
                    }
                }

                (button.layoutParams as? androidx.recyclerview.widget.RecyclerView.LayoutParams)?.apply {
                    setMargins(0, 8.dp.toInt(), 0, 0)
                }
            },
            itemList = config.visibleVariants
        ).also { binding.rvButtons.adapter = it }
    }

    private fun updateButtons() {
        buttonAdapter?.items = config.visibleVariants
    }

    private fun showConfigSheet() {
        val sheetBinding = BottomSheetButtonConfigBinding.inflate(
            LayoutInflater.from(this), null, false
        )

        sheetBinding.colorPalette.setColors(config.customColors, config.tintColor)
        sheetBinding.colorPalette.onColorSelected = { color ->
            config = config.copy(tintColor = color)
            updateButtons()
        }
        sheetBinding.colorPalette.onAddClicked = { ctx, selColor ->
            ColorPaletteSheet.show(
                context = ctx,
                title = "Add Color",
                selectedColor = selColor,
                onColorPicked = { color ->
                    config = config.copy(tintColor = color)
                    val newColors = if (config.customColors.contains(color)) config.customColors
                        else (listOf(color) + config.customColors).take(6)
                    config = config.copy(customColors = newColors)
                    updateButtons()
                    sheetBinding.colorPalette.setColors(newColors, color)
                }
            )
        }

        sheetBinding.swDestructive.isChecked = config.destructive
        sheetBinding.swDisabled.isChecked = config.disabled
        sheetBinding.swShimmer.isChecked = config.shimmer
        sheetBinding.slCornerRadius.value = config.cornerRadius
        sheetBinding.tvCornerRadiusValue.text = "${config.cornerRadius.toInt()}dp"

        val sizeFilters = listOf("All", "Small", "Medium", "Large")
        val chipGroup = sheetBinding.cgSizeFilter
        chipGroup.removeAllViews()
        val context = chipGroup.context
        sizeFilters.forEachIndexed { index, label ->
            val chip = Chip(context).apply {
                text = label
                isCheckable = true
                setTextAppearance(UIKitR.style.TextAppearance_Inter_Regular_B2)
                chipBackgroundColor = context.resources.getColorStateList(UIKitR.color.slr_chip_filter_bg, context.theme)
                setTextColor(context.resources.getColorStateList(UIKitR.color.slr_chip_filter_text, context.theme))
                chipStrokeColor = context.resources.getColorStateList(UIKitR.color.black_30, context.theme)
                chipStrokeWidth = context.resources.getDimensionPixelSize(UIKitR.dimen.dimen_1).toFloat()
                chipCornerRadius = context.resources.getDimension(UIKitR.dimen.xs)
                chipStartPadding = context.resources.getDimensionPixelSize(UIKitR.dimen.s).toFloat()
                chipEndPadding = context.resources.getDimensionPixelSize(UIKitR.dimen.s).toFloat()
                chipMinHeight = context.resources.getDimensionPixelSize(UIKitR.dimen.dimen_32).toFloat()
                isChecked = when (config.sizeFilter) {
                    SizeFilter.ALL -> index == 0
                    SizeFilter.SMALL -> index == 1
                    SizeFilter.MEDIUM -> index == 2
                    SizeFilter.LARGE -> index == 3
                }
            }
            chipGroup.addView(chip)
        }
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedChip = chipGroup.findViewById<Chip>(checkedIds[0])
                val index = chipGroup.indexOfChild(checkedChip)
                config = config.copy(
                    sizeFilter = when (index) {
                        0 -> SizeFilter.ALL
                        1 -> SizeFilter.SMALL
                        2 -> SizeFilter.MEDIUM
                        else -> SizeFilter.LARGE
                    }
                )
                updateButtons()
            }
        }

        setupMutualExclusiveToggles(sheetBinding)

        sheetBinding.slCornerRadius.addOnChangeListener { _, value, _ ->
            config = config.copy(cornerRadius = value)
            sheetBinding.tvCornerRadiusValue.text = "${value.toInt()}dp"
            updateButtons()
        }

        GuidelineConfigSheet(
            fragmentManager = supportFragmentManager,
            title = "Configure Button",
            contentView = sheetBinding.root
        ).show()
    }

    private fun setupMutualExclusiveToggles(sheetBinding: BottomSheetButtonConfigBinding) {
        sheetBinding.swDestructive.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(destructive = isChecked)
            if (isChecked) {
                sheetBinding.swDisabled.isChecked = false
                sheetBinding.swShimmer.isChecked = false
                config = config.copy(disabled = false, shimmer = false)
            }
            updateButtons()
        }

        sheetBinding.swDisabled.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(disabled = isChecked)
            if (isChecked) {
                sheetBinding.swDestructive.isChecked = false
                sheetBinding.swShimmer.isChecked = false
                config = config.copy(destructive = false, shimmer = false)
            }
            updateButtons()
        }

        sheetBinding.swShimmer.setOnCheckedChangeListener { _, isChecked ->
            config = config.copy(shimmer = isChecked)
            if (isChecked) {
                sheetBinding.swDestructive.isChecked = false
                sheetBinding.swDisabled.isChecked = false
                config = config.copy(destructive = false, disabled = false)
            }
            updateButtons()
        }
    }

    private fun createButton(context: Context): Button {
        return Button(context, null).apply {
            layoutParams = androidx.recyclerview.widget.RecyclerView.LayoutParams(
                androidx.recyclerview.widget.RecyclerView.LayoutParams.MATCH_PARENT,
                androidx.recyclerview.widget.RecyclerView.LayoutParams.WRAP_CONTENT
            )
        }
    }

    data class ButtonConfig(
        val destructive: Boolean = false,
        val disabled: Boolean = false,
        val shimmer: Boolean = false,
        val cornerRadius: Float = 8f,
        val sizeFilter: SizeFilter = SizeFilter.ALL,
        val tintColor: Int? = null,
        val customColors: List<Int> = defaultColors,
    ) {
        val visibleVariants: List<ButtonVariant>
            get() = when (sizeFilter) {
                SizeFilter.ALL -> allVariants.toList()
                else -> allVariants.filter { it.size == sizeFilter }
            }

        companion object {
            val defaultColors = listOf(
                "#1659AB".toColorInt(),
                "#FFAB00".toColorInt(),
                "#FF3030".toColorInt(),
                "#1A9F67".toColorInt(),
                "#18A4F2".toColorInt(),
                "#ED608F".toColorInt(),
                "#191919".toColorInt(),
                "#878F99".toColorInt(),
            )
        }
    }

    enum class SizeFilter { SMALL, MEDIUM, LARGE, ALL }

    data class ButtonVariant(
        val section: Section,
        val type: ButtonType,
        val size: SizeFilter,
        val label: String,
    )

    enum class Section { FILLED, SECONDARY, TERTIARY, TEXT }

    companion object {
        private fun variant(section: Section, type: ButtonType, size: SizeFilter, label: String) =
            ButtonVariant(section, type, size, label)

        private fun sized(section: Section, baseName: String, small: ButtonType, medium: ButtonType, large: ButtonType) = listOf(
            variant(section, small, SizeFilter.SMALL, "Small $baseName Button"),
            variant(section, medium, SizeFilter.MEDIUM, "Medium $baseName Button"),
            variant(section, large, SizeFilter.LARGE, "Large $baseName Button"),
        )

        val allVariants: List<ButtonVariant> = buildList {
            addAll(sized(Section.FILLED, "Primary", ButtonType.FILLED, ButtonType.FILLED_MEDIUM, ButtonType.FILLED_LARGE))
            addAll(sized(Section.SECONDARY, "Secondary", ButtonType.SECONDARY, ButtonType.SECONDARY_MEDIUM, ButtonType.SECONDARY_LARGE))
            addAll(sized(Section.TERTIARY, "Tertiary", ButtonType.TERTIARY, ButtonType.TERTIARY_MEDIUM, ButtonType.TERTIARY_LARGE))
            addAll(sized(Section.TEXT, "Text", ButtonType.TEXT, ButtonType.TEXT_MEDIUM, ButtonType.TEXT_LARGE))
        }
    }
}
