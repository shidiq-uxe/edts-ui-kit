package id.co.edtslib.edtsuikit.tmh

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.databinding.BottomSheetTmhRedemptionBinding
import id.co.edtslib.edtsuikit.databinding.ItemTmhRedemptionProductBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.button.Button
import id.co.edtslib.uikit.utils.TextStyleKey
import id.co.edtslib.uikit.utils.TextStyleProvider.get
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import kotlin.math.ceil
import id.co.edtslib.uikit.R as UIKitR

class TmhRedemptionBottomSheet : TmhBottomSheet() {

    private lateinit var fair: TmhFair
    private lateinit var selection: RedemptionSelection
    private var sheetBinding: BottomSheetTmhRedemptionBinding? = null
    private var productAdapter: BaseAdapter<ProductRow, ItemTmhRedemptionProductBinding>? = null
    private var pendingSaveResult: Bundle? = null
    private val productAnimations = mutableMapOf<View, TmhProductSelectionAnimator>()

    private fun renderSelection() {
        val content = sheetBinding ?: return
        val widestCounter = getString(R.string.tmh_counter, selection.maximum, selection.maximum)
        content.tvCounter.minimumWidth = ceil(Layout.getDesiredWidth(widestCounter, content.tvCounter.paint)).toInt() +
            content.tvCounter.compoundPaddingLeft + content.tvCounter.compoundPaddingRight
        content.tvCounter.text = getString(R.string.tmh_counter, selection.total, selection.maximum)

        content.btnSave.apply {
            setText(when (selection.action) {
                RedemptionSelection.Action.ADD -> R.string.tmh_add_cart
                RedemptionSelection.Action.UPDATE -> R.string.tmh_update_cart
                RedemptionSelection.Action.REMOVE -> R.string.tmh_remove_cart
            })
            isDestructive = selection.action == RedemptionSelection.Action.REMOVE
            icon = if (selection.action == RedemptionSelection.Action.ADD) requireContext().drawable(UIKitR.drawable.ic_plus) else null
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            iconPadding = 2.dp.toInt()
            isEnabled = selection.canSave && pendingSaveResult == null
        }
        productAdapter?.items = fair.products.map { product ->
            ProductRow(product, selection.quantity(product.id), selection.total)
        }
    }

    private fun bindProduct(row: ProductRow, binding: ItemTmhRedemptionProductBinding) {
        val product = row.product
        val quantity = selection.quantity(product.id)
        val isUnavailable = !product.isInStock || !fair.state.isEligible
        binding.ivProduct.setImageResource(product.imageRes)
        binding.ivProduct.setUnavailable(isUnavailable)
        binding.tvName.setText(product.nameRes)
        binding.tvPrice.setText(product.priceRes)
        binding.tvOriginalPrice.setText(product.originalPriceRes)
        binding.tvOriginalPrice.paintFlags = originalPriceFlags()
        binding.tvName.setTextColor(requireContext().color(
            if (isUnavailable) UIKitR.color.black_40 else UIKitR.color.black_70
        ))
        binding.tvPrice.setTextColor(requireContext().color(
            if (isUnavailable) UIKitR.color.black_40 else UIKitR.color.red_30
        ))
        binding.tvDiscount.setTextColor(requireContext().color(
            if (isUnavailable) UIKitR.color.black_40 else UIKitR.color.red_30
        ))
        binding.tvDiscount.backgroundTintList = ColorStateList.valueOf(requireContext().color(
            if (isUnavailable) UIKitR.color.black_20 else UIKitR.color.red_10
        ))
        productAnimations.getOrPut(binding.root) { TmhProductSelectionAnimator(binding) }
            .bind(product.id, quantity > 0)
        binding.tvTotal.isVisible = quantity > 0
        binding.tvTotal.text = buildHighlightedMessage(
            context = requireContext(),
            htmlMessage = getString(R.string.tmh_total_product, quantity),
            highlightedTextAppearance = TextStyleKey.B3_SEMIBOLD.get(requireContext(), UIKitR.color.black_70),
        )
        val isRestocking = !product.isInStock && fair.state.isEligible
        binding.tvRestock.isVisible = isRestocking
        binding.stepper.isVisible = !isRestocking
        binding.quantityContainer.setBackgroundColor(requireContext().color(
            if (isUnavailable) UIKitR.color.black_20 else UIKitR.color.info_background
        ))

        binding.root.tmhTypography()
        binding.stepper.apply {
            this.binding.countText.setTextAppearance(R.style.TextAppearance_Tmh_SemiBold_B2)
            TextViewCompat.setLineHeight(this.binding.countText, 16.dp.toInt())
            clearListener()
            setAllowTextEditing(false)
            isAlwaysExpanded = true
            isDragRepeatEnabled = true
            isStepperHapticFeedbackEnabled = true
            isDragFeedbackEnabled = true
            // A product can use only its own quantity plus the fair's remaining allowance.
            setMaxValue(quantity + selection.maximum - selection.total)
            setCount(quantity, animate = false)
            isEnabled = !isUnavailable
            setPlusButtonEnabled(selection.canIncrement(product.id))
            this.binding.plusButton.backgroundTintList = requireContext().colorStateList(UIKitR.color.slr_button_filled_bg)
            setMinusButtonEnabled(selection.canDecrement(product.id))
            this.binding.minusButton.iconTint = ColorStateList.valueOf(requireContext().color(
                if (selection.canDecrement(product.id)) UIKitR.color.primary_30 else UIKitR.color.black_30
            ))
            this.binding.countText.setTextColor(requireContext().color(
                if (isUnavailable) UIKitR.color.black_40 else UIKitR.color.black_60
            ))
            setOnCountChangeListener { quantity ->
                if (!selection.update(product.id, quantity)) {
                    setCount(selection.quantity(product.id), animate = false)
                }
                renderSelection()
            }
        }
    }

    override fun requestDismiss() {
        if (!selection.hasChanges) {
            closeSheet()
            return
        }
        restoreExpandedState()
        if (childFragmentManager.findFragmentByTag(TmhUnsavedBottomSheet.TAG) == null) {
            TmhUnsavedBottomSheet().show(childFragmentManager, TmhUnsavedBottomSheet.TAG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scenario = TmhScenario.valueOf(requireArguments().getString(ARG_SCENARIO) ?: TmhScenario.DEFAULT.name)
        fair = TmhFixtures.fairs(scenario).first { it.id == requireArguments().getString(ARG_FAIR_ID) }
        val original = requireArguments().getBundle(ARG_QUANTITIES)?.quantities().orEmpty()
        val actualState = TmhFairState.valueOf(requireNotNull(requireArguments().getString(ARG_FAIR_STATE)))
        fair = fair.copy(
            state = actualState,
            savedQuantities = original,
            products = fair.products.map {
                if (actualState == TmhFairState.OUT_OF_STOCK) it.copy(isInStock = false) else it
            },
        )
        selection = fair.selection()
        savedInstanceState?.getBundle(STATE_PENDING)?.let { pending ->
            fair.products.forEach { selection.update(it.id, 0) }
            pending.quantities().forEach { (id, quantity) -> selection.update(id, quantity) }
        }
        title = getString(fair.nameRes).takeIf { it.isNotBlank() } ?: getString(R.string.tmh_detail_fallback)
        childFragmentManager.setFragmentResultListener(TmhUnsavedBottomSheet.RESULT_DISCARD, this) { _, _ ->
            closeSheet()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val content = BottomSheetTmhRedemptionBinding.inflate(inflater, container, false)
        sheetBinding = content
        content.tvDescription.text = getString(R.string.tmh_description, getString(fair.nameRes))
        content.tvTerms.setOnClickListener {
            if (childFragmentManager.findFragmentByTag(TmhTermsBottomSheet.TAG) == null) {
                TmhTermsBottomSheet().show(childFragmentManager, TmhTermsBottomSheet.TAG)
            }
        }
        content.alertBox.isVisible = !fair.state.isEligible
        content.alertBox.bindFair(fair)
        content.alertBox.isActionVisible = false
        content.alertBox.alertBackgroundColor = requireContext().color(fair.alertBackgroundRes())
        content.btnSave.buttonType = Button.ButtonType.FILLED_LARGE
        content.btnSave.tmhTextAppearance(R.style.TextAppearance_Tmh_SemiBold_Button_Text_Large, 24)
        content.btnSave.setOnClickListener {
            if (selection.canSave && pendingSaveResult == null) {
                // TODO: Cross-device cart changes and save failures are outside this happy-flow prototype.
                // A future failure must retain pending quantities and offer Retry/Dismiss in an indefinite snackbar.
                pendingSaveResult = Bundle().apply {
                    putString(KEY_FAIR_ID, fair.id)
                    putBundle(KEY_QUANTITIES, selection.quantities.toQuantityBundle())
                }
                content.btnSave.isEnabled = false
                closeSheet()
            }
        }
        productAdapter = BaseAdapter.adapterOf<ProductRow, ItemTmhRedemptionProductBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, row, binding, _ -> bindProduct(row, binding) }),
            diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old.product.id == new.product.id },
                areContentsTheSame = { old, new -> old == new },
            ),
        )
        productAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) { refreshSheetSize() }
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) { refreshSheetSize() }
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) { refreshSheetSize() }
        })
        content.root.tmhTypography()
        // Keep the touched holder stable. Selection feedback is owned by the card animator.
        content.rvProducts.itemAnimator = null
        content.rvProducts.adapter = productAdapter
        renderSelection()
        contentView = content.root
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(STATE_PENDING, selection.quantities.toQuantityBundle())
        super.onSaveInstanceState(outState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        val result = pendingSaveResult
        pendingSaveResult = null
        val manager = parentFragmentManager
        super.onDismiss(dialog)
        // Reload the listing after the committed tray is dismissed, never on ordinary cancellation.
        result?.let { manager.setFragmentResult(RESULT_SAVE, it) }
    }

    override fun onDestroyView() {
        productAnimations.values.forEach { it.release() }
        productAnimations.clear()
        sheetBinding?.rvProducts?.adapter = null
        productAdapter = null
        sheetBinding = null
        super.onDestroyView()
    }

    private data class ProductRow(val product: TmhProduct, val quantity: Int, val total: Int)

    companion object {
        const val TAG = "TMH_REDEMPTION"
        const val RESULT_SAVE = "tmh_save_result"
        const val KEY_FAIR_ID = "fair_id"
        const val KEY_QUANTITIES = "quantities"
        private const val ARG_FAIR_ID = "fair_id"
        private const val ARG_SCENARIO = "scenario"
        private const val ARG_FAIR_STATE = "fair_state"
        private const val ARG_QUANTITIES = "original_quantities"
        private const val STATE_PENDING = "pending_quantities"

        fun newInstance(fair: TmhFair, scenario: TmhScenario): TmhRedemptionBottomSheet =
            TmhRedemptionBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_FAIR_ID, fair.id)
                    putString(ARG_SCENARIO, scenario.name)
                    putString(ARG_FAIR_STATE, fair.state.name)
                    putBundle(ARG_QUANTITIES, fair.savedQuantities.toQuantityBundle())
                }
            }
    }
}

fun Map<String, Int>.toQuantityBundle(): Bundle = Bundle().also { bundle ->
    forEach { (id, quantity) -> bundle.putInt(id, quantity) }
}

fun Bundle.quantities(): Map<String, Int> = keySet().associateWith { getInt(it) }
