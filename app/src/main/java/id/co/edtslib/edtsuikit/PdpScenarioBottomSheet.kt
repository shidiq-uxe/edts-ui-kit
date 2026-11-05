package id.co.edtslib.edtsuikit

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.co.edtslib.edtsuikit.databinding.BottomSheetPdpScenarioBinding
import id.co.edtslib.uikit.tray.BottomSheetTray

class PdpScenarioBottomSheet : BottomSheetTray() {

    private var sheetBinding: BottomSheetPdpScenarioBinding? = null
    private var discountScenario = PdpDiscountScenario.COLLAPSE_SPACE
    private var shouldPlayOnDismiss = false

    private fun updateDescription() {
        sheetBinding?.tvScenarioDescription?.setText(
            when (discountScenario) {
                PdpDiscountScenario.COLLAPSE_SPACE -> R.string.pdp_scenario_collapse_description
                PdpDiscountScenario.KEEP_SPACE -> R.string.pdp_scenario_keep_description
                PdpDiscountScenario.ANIMATE_SPACE -> R.string.pdp_scenario_animate_description
                PdpDiscountScenario.HIDE_DISCOUNTS_DURING_SPOTLIGHT -> R.string.pdp_scenario_reveal_karton_description
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val state = savedInstanceState ?: arguments
        val previousScenario = if (state?.getBoolean(KEEP_DISCOUNT_SPACE) == true) {
            PdpDiscountScenario.KEEP_SPACE
        } else {
            PdpDiscountScenario.COLLAPSE_SPACE
        }
        discountScenario = PdpDiscountScenario.fromSavedValue(state?.getString(SCENARIO), previousScenario)
        title = getString(R.string.pdp_scenario_title)
        dragHandleVisibility = true
        titleDividerVisibility = true
        shouldShowClose = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sheetBinding = BottomSheetPdpScenarioBinding.inflate(inflater, null, false)
        contentView = sheetBinding?.root
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = sheetBinding ?: return
        binding.rgDiscountSpace.check(
            when (discountScenario) {
                PdpDiscountScenario.COLLAPSE_SPACE -> R.id.rbCollapseSpace
                PdpDiscountScenario.KEEP_SPACE -> R.id.rbKeepSpace
                PdpDiscountScenario.ANIMATE_SPACE -> R.id.rbAnimateSpace
                PdpDiscountScenario.HIDE_DISCOUNTS_DURING_SPOTLIGHT -> R.id.rbRevealKartonOnDismiss
            }
        )
        updateDescription()
        binding.rgDiscountSpace.setOnCheckedChangeListener { _, checkedId ->
            discountScenario = when (checkedId) {
                R.id.rbKeepSpace -> PdpDiscountScenario.KEEP_SPACE
                R.id.rbAnimateSpace -> PdpDiscountScenario.ANIMATE_SPACE
                R.id.rbRevealKartonOnDismiss -> PdpDiscountScenario.HIDE_DISCOUNTS_DURING_SPOTLIGHT
                else -> PdpDiscountScenario.COLLAPSE_SPACE
            }
            updateDescription()
        }
        binding.btnPlaySpotlight.setOnClickListener {
            shouldPlayOnDismiss = true
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SCENARIO, discountScenario.name)
        super.onSaveInstanceState(outState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (shouldPlayOnDismiss) {
            shouldPlayOnDismiss = false
            parentFragmentManager.setFragmentResult(
                RESULT_SCENARIO,
                Bundle().apply { putString(SCENARIO, discountScenario.name) }
            )
        }
    }

    override fun onDestroyView() {
        sheetBinding = null
        contentView = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "PDP_SCENARIO_SHEET"
        const val RESULT_SCENARIO = "pdp_spotlight_scenario"
        const val SCENARIO = "discount_scenario"
        private const val KEEP_DISCOUNT_SPACE = "keep_discount_space"

        fun newInstance(scenario: PdpDiscountScenario): PdpScenarioBottomSheet =
            PdpScenarioBottomSheet().apply {
                arguments = Bundle().apply { putString(SCENARIO, scenario.name) }
            }
    }
}
