package id.co.edtslib.edtsuikit

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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
import id.co.edtslib.uikit.core.chip.CHIP_STATE_SELECTED
import id.co.edtslib.uikit.core.chip.ChipStateAnnotation
import id.co.edtslib.uikit.core.chipgroup.ChipGroupAdapter
import id.co.edtslib.uikit.core.chipgroup.ChipGroupDelegate
import id.co.edtslib.uikit.R as UIKitR
import id.co.edtslib.uikit.core.chipgroup.ChipGroup.ChipGroupSelectionMode
import id.co.edtslib.uikit.utils.dp

private data class ChipGroupItem(
    val id: Int,
    val label: String,
    @ChipStateAnnotation val state: Int,
)

class GuidelinesChipActivity : GuidelinesBaseActivity() {
    private val binding by viewBinding<ActivityGuidelinesChipBinding>()
    override val hasConfigMenu = true
    private var config = ChipConfig()
    private var chipGroupConfig = ChipGroupConfig()
    private lateinit var chipGroupCoreAdapter: ChipGroupAdapter<ChipGroupItem>
    private lateinit var chipGroupKlikSuggestionAdapter: ChipGroupAdapter<ChipGroupItem>
    private lateinit var chipGroupKlikFiltersAdapter: ChipGroupAdapter<ChipGroupItem>
    private lateinit var chipGroupPoinkuAdapter: ChipGroupAdapter<ChipGroupItem>
    private lateinit var chipGroupPreviewAdapter: ChipGroupAdapter<ChipGroupItem>
    private var chipGroupPreviewProduct: ChipProduct? = null
    private var chipGroupPreviewVariantIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = savedInstanceState?.getBundle(STATE_CONFIG)?.toChipConfig() ?: ChipConfig()
        chipGroupConfig = savedInstanceState?.getBundle(STATE_CHIP_GROUP_CONFIG)?.toChipGroupConfig() ?: ChipGroupConfig()

        supportFragmentManager.setFragmentResultListener(
            ChipConfigBottomSheet.RESULT_CONFIG,
            this,
        ) { _, result ->
            config = result.toChipConfig()
            updatePreview()
        }

        supportFragmentManager.setFragmentResultListener(
            ChipConfigBottomSheet.RESULT_CHIP_GROUP_CONFIG,
            this,
        ) { _, result ->
            chipGroupConfig = result.toChipGroupConfig()
            applyChipGroupPreviewConfig()
        }

        setContentView(binding.root)
        setupCoreDefault()
        setupKlikStates()
        setupKlikFiltersStates()
        setupPoinkuStates()
        setupStaticChipGroups()
        setupChipGroupPreview()
        updatePreview()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(STATE_CONFIG, config.toBundle())
        outState.putBundle(STATE_CHIP_GROUP_CONFIG, chipGroupConfig.toBundle())
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

    private fun setupStaticChipGroups() = with(binding) {
        chipGroupCoreAdapter = createChipGroupAdapter { CoreChip(it.context) }
        chipGroupKlikSuggestionAdapter = createChipGroupAdapter {
            KlikChip(it.context).apply { klikChipVariant = KlikChipVariant.Suggestion }
        }
        chipGroupKlikFiltersAdapter = createChipGroupAdapter {
            KlikChip(it.context).apply { klikChipVariant = KlikChipVariant.Filters }
        }
        chipGroupPoinkuAdapter = createChipGroupAdapter { PoinkuChip(it.context) }

        chipGroupCore.chipAdapter = chipGroupCoreAdapter
        chipGroupKlikSuggestion.chipAdapter = chipGroupKlikSuggestionAdapter
        chipGroupKlikFilters.chipAdapter = chipGroupKlikFiltersAdapter
        chipGroupPoinku.chipAdapter = chipGroupPoinkuAdapter

        val items = (1..STATIC_ITEM_COUNT).map { index ->
            ChipGroupItem(id = index, label = "Chip $index", state = CHIP_STATE_IDLE)
        }
        chipGroupCoreAdapter.submitList(items)
        chipGroupKlikSuggestionAdapter.submitList(items)
        chipGroupKlikFiltersAdapter.submitList(items)
        chipGroupPoinkuAdapter.submitList(items)
    }

    private fun setupChipGroupPreview() {
        applyChipGroupPreviewConfig()
    }

    private fun createChipGroupAdapter(
        selectionMode: () -> ChipGroupSelectionMode = { ChipGroupSelectionMode.MULTIPLE },
        createChip: (parent: ViewGroup) -> CoreChip,
    ): ChipGroupAdapter<ChipGroupItem> {
        lateinit var adapter: ChipGroupAdapter<ChipGroupItem>
        adapter = ChipGroupAdapter(
            createChip = createChip,
            bindChip = { chip, item, _ ->
                chip.text = item.label
                chip.state = item.state
            },
            areItemsTheSame = { old, new -> old.id == new.id },
            isSelected = { it.state == CHIP_STATE_SELECTED },
            delegate = object : ChipGroupDelegate<ChipGroupItem> {
                override fun onChipClick(chip: CoreChip, item: ChipGroupItem, position: Int) {
                    val current = adapter.currentList()
                    val isReselecting = item.state == CHIP_STATE_SELECTED

                    val updated = when (selectionMode()) {
                        ChipGroupSelectionMode.SINGLE -> current.map {
                            val newState = when {
                                it.id != item.id -> CHIP_STATE_IDLE
                                isReselecting -> CHIP_STATE_IDLE
                                else -> CHIP_STATE_SELECTED
                            }
                            it.copy(state = newState)
                        }
                        ChipGroupSelectionMode.MULTIPLE -> current.map {
                            if (it.id != item.id) return@map it
                            it.copy(state = if (isReselecting) CHIP_STATE_IDLE else CHIP_STATE_SELECTED)
                        }
                    }
                    adapter.submitList(updated)
                }
            },
        )
        return adapter
    }

    private fun applyChipGroupPreviewConfig() = with(binding) {
        if (!::chipGroupPreviewAdapter.isInitialized ||
            chipGroupPreviewProduct != chipGroupConfig.product ||
            chipGroupPreviewVariantIndex != chipGroupConfig.variantIndex
        ) {
            chipGroupPreviewProduct = chipGroupConfig.product
            chipGroupPreviewVariantIndex = chipGroupConfig.variantIndex
            chipGroupPreviewAdapter = createChipGroupAdapter(
                createChip = { parent ->
                    createChipForProduct(chipGroupConfig.product, chipGroupConfig.variantIndex, parent.context)
                },
                selectionMode = { chipGroupConfig.selectionMode },
            )
            chipGroupPreview.chipAdapter = chipGroupPreviewAdapter
        }

        val items = (1..chipGroupConfig.itemCount).map { index ->
            ChipGroupItem(id = index, label = "Chip $index", state = CHIP_STATE_IDLE)
        }
        chipGroupPreviewAdapter.submitList(items)

        chipGroupPreview.orientation = chipGroupConfig.orientation
        chipGroupPreview.spanCount = chipGroupConfig.spanCount
        chipGroupPreview.itemSpacingHorizontal = chipGroupConfig.itemSpacingHorizontalDp.dp.toInt()
        chipGroupPreview.itemSpacingVertical = chipGroupConfig.itemSpacingVerticalDp.dp.toInt()
        chipGroupPreview.selectionMode = chipGroupConfig.selectionMode
    }

    private fun createChipForProduct(product: ChipProduct, variantIndex: Int, context: Context): CoreChip =
        when (product) {
            ChipProduct.CORE -> CoreChip(context)
            ChipProduct.KLIK -> KlikChip(context).apply {
                klikChipVariant = if (variantIndex == 1) KlikChipVariant.Filters else KlikChipVariant.Suggestion
            }
            ChipProduct.POINKU -> PoinkuChip(context)
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
        ChipConfigBottomSheet.newInstance(config, chipGroupConfig)
            .show(supportFragmentManager, ChipConfigBottomSheet.TAG)
    }

    companion object {
        private const val STATE_CONFIG = "chip_config_state"
        private const val STATE_CHIP_GROUP_CONFIG = "chip_group_config_state"
        private const val STATIC_ITEM_COUNT = 10
    }
}